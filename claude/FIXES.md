# Исправления по результатам code review

## 1. Производительность

### JwtUtil — кэширование SecretKey
**Файл:** `src/main/java/backend/saferent/util/JwtUtil.java`

**Проблема:** Методы `generateToken` и `extractClaims` вызывали `Keys.hmacShaKeyFor(secret.getBytes())` при каждом обращении. Ключ пересчитывался на каждый HTTP-запрос с авторизацией.

**Исправление:** Добавлен `@PostConstruct`-метод `init()`, который вычисляет `SecretKey` один раз при запуске приложения и сохраняет в поле `secretKey`. Оба метода теперь используют это поле.

---

### ContractServiceImpl — два запроса вместо одного + сломанный distinct
**Файл:** `src/main/java/backend/saferent/service/impl/ContractServiceImpl.java`  
**Файл:** `src/main/java/backend/saferent/repository/ContractRepository.java`

**Проблема:** `getMyContracts()` выполнял два отдельных запроса (`findByTenant` и `findByLandlord`), объединял результаты через `addAll`, а затем вызывал `.distinct()`. Однако `.distinct()` на JPA-сущностях без переопределённых `equals`/`hashCode` использует ссылочное равенство — дедупликация не работала.

**Исправление:** В `ContractRepository` добавлен JPQL-запрос:
```java
@Query("SELECT c FROM Contract c WHERE c.tenant = :user OR c.landlord = :user")
List<Contract> findByTenantOrLandlord(@Param("user") User user);
```
`getMyContracts()` теперь делает один запрос. Попутно убрана лишняя загрузка `User` из БД — теперь используется `securityUtils.getCurrentUser()`, который возвращает сущность прямо из `SecurityContext`.

---

### InspectionServiceImpl — мёртвая проверка статуса
**Файл:** `src/main/java/backend/saferent/service/impl/InspectionServiceImpl.java`

**Проблема:** В `uploadCheckinPhoto()` после вызова `getActiveContractOrThrow()` стояла повторная проверка `contract.getStatus() != ContractStatus.ACTIVE`. Она никогда не могла выполниться: `getActiveContractOrThrow` уже бросает исключение, если контракт не активен.

**Исправление:** Мёртвый блок удалён.

---

## 2. Корректность логики

### PaymentServiceImpl — недостижимая ветка в getEscrowStatus
**Файл:** `src/main/java/backend/saferent/service/impl/PaymentServiceImpl.java`

**Проблема:** Переменная `depositPaid` вычислялась как `deposit != null && deposit.getStatus() == PAID`. Следующая ветка `else if (deposit.getStatus() == REFUNDED)` была недостижима: если `depositPaid == true`, то статус уже `PAID`, а не `REFUNDED`. Ветка `RETURNED` никогда не срабатывала.

**Исправление:** Логика переписана в правильном порядке — сначала проверяется `deposit == null`, затем `REFUNDED`, затем статус контракта:
```
deposit == null           → NOT_PAID
deposit.status == REFUNDED → RETURNED
contract.status == COMPLETED → TENANT
contract.status == CANCELLED → PENDING_REVIEW
иначе                     → ESCROW
```

---

## 3. Дублирование кода

### AuthServiceImpl — дублированный builder AuthResponse
**Файл:** `src/main/java/backend/saferent/service/impl/AuthServiceImpl.java`

**Проблема:** Методы `register()` и `login()` содержали идентичный блок `AuthResponse.builder()...build()` из 7 строк каждый.

**Исправление:** Извлечен приватный метод `toAuthResponse(User user, String token)`. Оба метода теперь вызывают его.

Также исправлено использование полного имени `java.time.LocalDateTime.now()` в `login()` — добавлен нормальный `import java.time.LocalDateTime`.

---

## 4. Лишние комментарии

Удалены комментарии, объясняющие *что* делает код (это уже понятно из имён методов и полей).

### Репозитории
Из следующих файлов удалены однострочные комментарии над каждым методом:
- `BookingRepository.java`
- `ChatRepository.java`
- `FavoriteRepository.java`
- `MessageRepository.java`
- `NotificationRepository.java`
- `PaymentRepository.java`
- `ReviewRepository.java`
- `InspectionPhotoRepository.java`

### Сущности и DTO
- `Payment.java` — убраны trailing-комментарии на полях (`// DEPOSIT или RENT`, `// KASPI, HALYK, CARD` и др.)
- `EscrowStatusResponse.java` — убраны комментарии-подсказки на полях
- `InspectionCompareResponse.java` — убраны комментарии над каждым полем
- `InspectionPhoto.java` — удалён комментарий `// InspectionPhoto.java` в первой строке файла

### Сервисы
- `PaymentServiceImpl.java` — удалены два комментария `// Берём requestedBy из токена`
- `ChatServiceImpl.java` — удалён навигационный комментарий `// ChatServiceImpl.java — только метод sendMessage`

---

## 5. AI-сервис

### ai_service/main.py — таймаут на загрузку изображений
**Проблема:** `urllib.request.urlopen(url)` вызывался без таймаута. При зависании удалённого хоста рабочий поток FastAPI блокировался бесконечно, что могло исчерпать пул потоков под нагрузкой.

**Исправление:** Добавлен `timeout=10` секунд:
```python
resp = urllib.request.urlopen(url, timeout=10)
```

Также удалены все очевидные русскоязычные комментарии (`# Приводим к одному размеру`, `# Конвертируем в grayscale`, `# SSIM` и т.д.) — поведение кода полностью понятно из вызываемых функций.

---

## Что не исправлялось (архитектурные изменения)

Следующие проблемы обнаружены, но отложены как требующие более широкого рефакторинга:

| Проблема | Причина откладывания |
|---|---|
| `userRepository.findById` дублируется во всех сервисах | Нужна инъекция `UserService` повсюду, риск циклических зависимостей |
| `getActiveContractOrThrow` продублирован в `PaymentServiceImpl` и `InspectionServiceImpl` | Требует нового метода в `ContractService` и перестройки зависимостей |
| 6 параметров у `NotificationService.create()` | Изменение публичного API — нужен `NotificationRequest` DTO |
| `payDeposit` и `payRent` почти идентичны | Сложная бизнес-логика, высокий риск регрессии при объединении |
| AI-вызовы выполняются последовательно в цикле по комнатам | Нужен `CompletableFuture` + разбиение транзакции |
| `getMyChats` делает N+2 запросов на чат | Нужен JPQL-запрос с проекцией |
| История сообщений без пагинации | Изменение публичного API контроллера |
| N+1 в `ApartmentMapper` (запрос к БД внутри маппера) | Нужен рефакторинг слоя сервис → маппер |
