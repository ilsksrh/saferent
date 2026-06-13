# SafeRent — Серверная часть (Backend). Техническая документация

> Документ для дипломной работы. Описывает реализованную серверную часть платформы
> аренды жилья **SafeRent**: применённые технологии, архитектуру, доменную модель,
> бизнес-логику ключевых модулей, перечень REST-эндпоинтов, а также раздел
> «Что замокано / что предстоит реализовать».

---

## 1. Назначение системы

**SafeRent** — веб-платформа долгосрочной аренды квартир (на примере г. Алматы),
которая делает сделку между арендатором и арендодателем безопасной за счёт:

- проверки (верификации) объявлений модератором перед публикацией;
- эскроу-хранения залога (депозит замораживается платформой, а не уходит напрямую владельцу);
- автоматического сравнения состояния квартиры на въезде и выезде с помощью ИИ
  (анализ фотографий), от которого зависит судьба залога;
- системы жалоб и модерации;
- полнотекстового поиска по объявлениям с устойчивостью к опечаткам.

Серверная часть — это REST API на Spring Boot, к которому обращается
одностраничное React-приложение (см. отдельный документ `SafeRent_Frontend.md`).

---

## 2. Технологический стек

| Слой | Технология | Версия | Роль в проекте |
|------|------------|--------|----------------|
| Язык | Java | 21 | Основной язык серверной части |
| Каркас | Spring Boot | 4.0.3 | Автоконфигурация, DI-контейнер, встроенный сервер |
| Web | Spring Web (MVC) | — | REST-контроллеры, обработка HTTP |
| Безопасность | Spring Security | 7.0.3 | Аутентификация JWT, авторизация по ролям, method security |
| Доступ к данным | Spring Data JPA + Hibernate | — | ORM, репозитории |
| СУБД | PostgreSQL | 15 | Основное хранилище |
| Поиск | Spring Data Elasticsearch | 6.0.3 (клиент 9.x) | Полнотекстовый поиск с fuzzy-логикой |
| Поисковый движок | Elasticsearch | 9.1.0 | Индекс объявлений |
| Объектное хранилище | MinIO (S3-совместимое) | сервер latest, клиент 8.5.11 | Хранение фото квартир, аватаров, фото осмотра |
| Токены | JJWT (io.jsonwebtoken) | 0.12.3 | Генерация и проверка JWT |
| Маппинг DTO | MapStruct | 1.6.0 | Преобразование Entity ↔ DTO |
| Boilerplate | Lombok | 1.18.34 | Геттеры/сеттеры/билдеры |
| Почта | Spring Mail + MailHog | — | Отправка OTP-кодов (в dev — фейковый SMTP) |
| Документация API | springdoc-openapi (Swagger UI) | 2.8.8 | Автогенерация описания REST API |
| ИИ-сервис | Python FastAPI + OpenCV + scikit-image | — | Сравнение фотографий (метрика SSIM) |
| Real-time | Spring WebSocket + STOMP (SockJS) | — | Живые сообщения чата и push-уведомления без перезагрузки |
| Контейнеризация | Docker / Docker Compose | — | Запуск инфраструктуры (БД, MinIO, ES, MailHog, AI) и самого бэкенда |

### Почему именно так

- **Spring Boot** даёт промышленный каркас с готовой безопасностью, валидацией и
  транзакциями — это снижает объём «инфраструктурного» кода и позволяет
  сконцентрироваться на бизнес-логике.
- **JWT (stateless)** выбран вместо серверных сессий, потому что фронтенд — это
  отдельное SPA: токен хранится у клиента и передаётся в заголовке `Authorization`,
  сервер не хранит состояние сессии.
- **Elasticsearch** отдельно от PostgreSQL, потому что SQL `LIKE` не умеет
  ранжировать результаты и устойчиво обрабатывать опечатки; ES даёт fuzzy-поиск
  «из коробки».
- **MinIO** вместо хранения файлов в БД/на диске — это S3-совместимое хранилище:
  бинарные файлы лежат отдельно, отдаются по прямой ссылке, легко масштабируется.
- **Отдельный Python-микросервис для ИИ**, потому что зрелые библиотеки
  компьютерного зрения (OpenCV, scikit-image) — это экосистема Python; Java-сервис
  обращается к нему по HTTP.

---

## 3. Архитектура

### 3.1 Слоистая архитектура (внутри Spring-приложения)

```
HTTP-запрос
   │
   ▼
Controller   ← REST-эндпоинты, валидация входа (@Valid), @PreAuthorize
   │
   ▼
Service (интерфейс) + ServiceImpl   ← бизнес-логика, @Transactional
   │
   ├──► Repository (Spring Data JPA)  → PostgreSQL
   ├──► ApartmentSearchService        → Elasticsearch
   ├──► FileStorageService            → MinIO
   ├──► NotificationService           → уведомления в БД
   ├──► AiAnalysisClient              → HTTP → Python AI-сервис
   └──► Mapper (MapStruct)            → Entity ↔ DTO
```

Каждый доменный модуль состоит из пяти типовых частей:
**Entity → Repository → Service/ServiceImpl → Controller → DTO + Mapper**.
Контроллер никогда не работает с сущностью напрямую — только через сервис и
получает/отдаёт DTO. Это разделяет внутреннюю модель БД и внешний контракт API.

### 3.2 Микросервисное окружение (Docker Compose, `docker-compose.dev.yml`)

| Сервис | Контейнер | Порт | Назначение |
|--------|-----------|------|------------|
| postgres | saferent-postgres | 5432 | База данных |
| minio | saferent-minio | 9000 / 9001 (консоль) | Хранилище файлов |
| minio-init | saferent-minio-init | — | Одноразовое создание бакета `saferent-photos` с публичным чтением |
| mailhog | saferent-mailhog | 1025 (SMTP) / 8025 (UI) | Перехват писем с OTP в режиме разработки |
| ai-service | saferent-ai | 8000 | FastAPI-сервис сравнения фото |
| elasticsearch | saferent-elasticsearch | 9200 | Поисковый индекс |

Само Spring-приложение в dev-режиме запускается из IntelliJ IDEA и обращается к
этим контейнерам по `localhost`.

---

## 4. Доменная модель (сущности)

Все сущности наследуют **`AbstractEntity`** — базовый класс с полями:
`id` (UUID, генерируется), `createdAt`, `updatedAt` (проставляются автоматически
через `@PrePersist` / `@PreUpdate`).

| Сущность | Назначение | Ключевые поля |
|----------|-----------|---------------|
| **User** | Пользователь | phone, email, name, passwordHash, preferredRole (TENANT/LANDLORD), `verified`, `admin`, avatarUrl, lastLoginAt |
| **District** | Район города | name, safetyScore, comfortScore, colorCode |
| **DistrictRating** | Оценка района пользователем | district, user, баллы |
| **Apartment** | Объявление | landlord, district, title, description, address, price, area, rooms, availableFrom, `verified`, `rejectionReason`, `rejectedAt`, status (ACTIVE/RENTED/ARCHIVED/DRAFT), `deletedAt` (мягкое удаление) |
| **ApartmentPhoto** | Фото квартиры | apartment, url (ссылка в MinIO), position |
| **Booking** | Заявка на бронь | apartment, tenant, startDate, endDate, message, status (PENDING/APPROVED/REJECTED/CANCELLED) |
| **Contract** | Договор аренды | tenant, landlord, apartment, rentAmount, depositAmount, status (DRAFT/PENDING/ACTIVE/COMPLETED/CANCELLED), tenantSigned, landlordSigned |
| **Payment** | Платёж | contract, type (DEPOSIT/RENT), amount, method (KASPI/HALYK/CARD/OTHER), status (PENDING/PAID/FAILED/REFUNDED), transactionId, paidAt |
| **InspectionPhoto** | Фото осмотра | contract, type (CHECKIN/CHECKOUT), url, roomLabel, ssimScore, damageDescription |
| **Chat** | Диалог | tenant, landlord, apartment |
| **Message** | Сообщение | chat, sender, text, isRead |
| **Notification** | Уведомление | user, title, message, type, relatedEntityId, isRead |
| **Review** | Отзыв | contract, author, targetUser, rating, comment |
| **Favorite** | Избранное | user, apartment |
| **Report** | Жалоба | reporter, targetUser/apartment/message, reason (SPAM/SCAM/FAKE_LISTING/ABUSE/OTHER), description, status (OPEN/IN_REVIEW/RESOLVED/REJECTED), moderator, resolutionComment |

---

## 5. Ключевые модули и методы

### 5.1 Аутентификация и авторизация

**Файлы:** `AuthController`, `AuthServiceImpl`, `JwtFilter`, `JwtUtil`,
`SecurityConfig`, `OtpServiceImpl`, `SecurityUtils`.

- **Регистрация** (`POST /api/v1/auth/register`): создаётся пользователь, пароль
  хешируется `BCrypt`, на email отправляется OTP-код (через `OtpService`).
- **Верификация аккаунта по OTP**: пользователь вводит код из письма; при успехе
  `verified = true`. В dev-режиме письма перехватывает MailHog (UI на `:8025`).
- **Вход** (`POST /api/v1/auth/login`): проверка пароля, генерация JWT
  (`JwtUtil`), в ответе — `accessToken`, `role`, `verified`, `admin`.
- **`JwtFilter`** на каждый запрос достаёт токен из заголовка `Authorization:
  Bearer ...`, валидирует подпись и формирует список прав:
  `ROLE_{preferredRole}` и дополнительно `ROLE_ADMIN`, если `user.isAdmin()`.
- **`SecurityConfig`**: stateless-сессии, CORS для `localhost:5173/3000/4173`,
  `@EnableMethodSecurity(prePostEnabled=true)` для аннотаций `@PreAuthorize`.
  Публичные эндпоинты: регистрация/логин, просмотр активных объявлений
  (`/apartments/active`, `/apartments/search`, `/apartments/{id}`), Swagger.
- **`SecurityUtils.getCurrentUserId()`** — извлекает id текущего пользователя из
  контекста безопасности; используется во всех сервисах для проверки прав.

### 5.2 Объявления и модерация

**Файлы:** `ApartmentController`, `ApartmentServiceImpl`, `ApartmentMapper`,
`ApartmentRepository`.

- **Создание** (`POST /api/v1/apartments`): `landlordId` берётся не из тела
  запроса, а из JWT (`@AuthenticationPrincipal`) — арендодатель не может создать
  объявление от чужого имени. Новое объявление по умолчанию `verified = false`
  (на модерации) и **не попадает в общий листинг**.
- **Листинг** (`GET /api/v1/apartments/active`): метод
  `findAllByStatusAndVerifiedTrueAndDeletedAtIsNull` — показываются только
  верифицированные, активные и не удалённые объявления.
- **Верификация модератором** (`POST /apartments/{id}/verify`,
  `@PreAuthorize("hasRole('ADMIN')")`): `verified = true`, очищается причина
  отклонения, объявление индексируется в Elasticsearch, арендодателю уходит
  уведомление.
- **Отклонение** (`POST /apartments/{id}/reject`, только ADMIN): модератор
  указывает `rejectionReason` (выбор причины + комментарий), проставляется
  `rejectedAt`, владельцу уходит уведомление с причиной.
- **Очередь модерации** (`GET /apartments/pending`, только ADMIN): список
  объявлений, ожидающих проверки.
- **Загрузка фото** (`POST /apartments/{id}/photos`, multipart/form-data): файл
  через `FileStorageService` уходит в MinIO, создаётся `ApartmentPhoto` со ссылкой.
  Доступ проверяет `isOwnerOrAdmin()` (владелец объявления **или** админ).
- **Удаление фото** (`DELETE /apartments/{id}/photos/{photoId}`): удаляет связь и
  объект; права — владелец или админ.
- **CRUD админом**: благодаря `isOwnerOrAdmin()` модератор может редактировать и
  удалять любые объявления.

### 5.3 Полнотекстовый поиск (Elasticsearch)

**Файлы:** `ApartmentSearchService`, `ApartmentDocument`,
`ApartmentSearchRepository`.

- В индекс **попадают только** верифицированные + активные + не удалённые
  объявления; при изменении статуса документ удаляется из индекса.
- **`search()`** строит `BoolQuery`:
  - `multiMatch` с `fuzziness("AUTO")` по полям `title^3`, `description`,
    `address^2` — поиск устойчив к опечаткам, заголовок весит больше;
  - фильтр `status = ACTIVE`;
  - дополнительные фильтры: диапазон цены, район (`districtId`), число комнат.
- **`reindexAll()`** — полная переиндексация (вызывается при старте из
  `DataLoader`).
- Сервис спроектирован «мягко»: через `ObjectProvider` он не падает, если
  Elasticsearch недоступен, — приложение продолжает работать без поиска.

### 5.4 Хранение файлов (MinIO)

**Файлы:** `FileStorageServiceImpl`, `MinioConfig`, `FileController`.

- При старте (`ApplicationReadyEvent`) создаётся бакет `saferent-photos` с
  публичным чтением.
- `FileStorageService.upload(...)` кладёт файл и возвращает публичную ссылку
  (`localhost:9000/saferent-photos/...`), которая сохраняется в БД.
- Через MinIO работают **все** загрузки: фото квартир, аватары пользователей,
  фото осмотра (check-in / check-out).

### 5.5 Бронирование и договоры

**Файлы:** `BookingServiceImpl`, `ContractServiceImpl`.

- Арендатор отправляет заявку (`Booking`), арендодатель её подтверждает/отклоняет.
- На основе подтверждённой брони создаётся договор (`Contract`) с суммами аренды
  и залога; договор подписывается обеими сторонами (`tenantSigned`,
  `landlordSigned`), после чего переходит в статус `ACTIVE`.

### 5.6 Платежи и эскроу залога

**Файл:** `PaymentServiceImpl`.

- **`payDeposit`**: только арендатор, договор должен быть `ACTIVE`, сумма обязана
  точно совпадать с `depositAmount`, повторная оплата запрещена. Депозит
  фиксируется как `PAID` и считается «замороженным в эскроу». Уведомления уходят
  обеим сторонам.
- **`payRent`**: оплата аренды возможна только после оплаты залога.
- **`getEscrowStatus`**: рассчитывает текущий «адрес» залога — `ESCROW` (заморожен),
  `TENANT` (возврат арендатору), `RETURNED`, `PENDING_REVIEW` и т. д.
- **`releaseDepositToTenant` / `releaseDepositToLandlord`**: высвобождение залога
  по итогам осмотра; договор переводится в `COMPLETED`, статус платежа —
  `REFUNDED`, обе стороны получают уведомления.
- Идентификатор транзакции генерируется как `TXN_xxxxxxxx`.

> **Важно для защиты:** реального списания денег нет — платёж сразу помечается
> `PAID`. Это симуляция платёжного шлюза (см. раздел 8).

### 5.7 Осмотр квартиры и ИИ-анализ

**Файлы:** `InspectionServiceImpl`, `AiAnalysisClient`, `ai_service/main.py`.

Логика «безопасного залога» — центральная фишка проекта:

1. **Check-in**: при заселении арендатор загружает фото комнат (`roomLabel`).
2. **Check-out**: при выселении загружает фото тех же комнат (нельзя без check-in).
3. **`compare()`**: для каждой комнаты пары «до/после» отправляются в ИИ-сервис.
   - Java-клиент `AiAnalysisClient.comparePhotos(beforeUrl, afterUrl)` делает
     HTTP-запрос к FastAPI (`POST /compare`).
   - **Python-сервис** (`main.py`) скачивает оба изображения, приводит к одному
     размеру, переводит в оттенки серого и считает метрику **SSIM** (Structural
     Similarity Index) через `scikit-image`; дополнительно через OpenCV находит
     контуры зон различий (`damageRegionCount`).
   - **Решение по залогу** на основе средней SSIM:
     - `≥ 0.92` → **NO_DAMAGE** → залог возвращается арендатору;
     - `0.70–0.92` → **MINOR_DAMAGE** → ручная проверка модератором;
     - `< 0.70` → **MAJOR_DAMAGE** → залог переходит арендодателю.
   - Результат (`ssimScore`, описание) сохраняется в `InspectionPhoto`, обеим
     сторонам уходят уведомления.

**Тонкость с сетью (решённая проблема):** в БД ссылки на фото содержат публичный
хост `localhost:9000`, но из контейнера AI `localhost` — это сам контейнер.
Поэтому `rewrite_url()` подменяет `localhost:9000` → `minio:9000` (имя сервиса в
docker-сети), и ИИ реально читает фото из MinIO.

### 5.8 Чат, уведомления, отзывы, избранное

- **Чат** (`ChatServiceImpl`): диалог привязан к паре «арендатор–арендодатель» +
  квартира; подсчёт непрочитанных сообщений.
- **Уведомления** (`NotificationServiceImpl`): единый метод `create(...)`
  используется всеми модулями (платежи, осмотр, модерация и т. д.).
- **Отзывы** (`ReviewServiceImpl`): рейтинг арендодателя считается как среднее по
  отзывам; есть агрегированный `UserRating`.
- **Избранное** (`FavoriteServiceImpl`): добавление/удаление, проверка наличия.

### 5.9 Жалобы и модерация (Report)

**Файлы:** `ReportController`, `ReportServiceImpl`, `ReportRepository`.

- `POST /api/v1/reports` — пользователь жалуется на объявление/пользователя/сообщение.
- `GET /reports/my` — свои жалобы; `GET /reports?status=...` — очередь модерации.
- `POST /reports/{id}/resolve` — модератор закрывает жалобу с комментарием
  (`RESOLVED` / `REJECTED`).

### 5.10 Админ-панель (серверная часть)

**Файл:** `AdminController` (`/api/v1/admin`, весь класс под
`@PreAuthorize("hasRole('ADMIN')")`).

- `GET /admin/users` — постраничный список пользователей с поиском по имени/телефону.
- `PATCH /admin/users/{id}/admin` — назначить/снять права администратора.
  Защита: администратор **не может снять права с самого себя**
  (`currentId.equals(id)` → `400`).

### 5.11 Real-time через WebSocket (STOMP)

**Файлы:** `WebSocketConfig`, `StompAuthChannelInterceptor`, изменения в
`ChatServiceImpl` и `NotificationServiceImpl`.

Чтобы интерфейс обновлялся «вживую» (новое сообщение в чате и любое уведомление
появляются без перезагрузки страницы), добавлен слой WebSocket поверх STOMP:

- **`WebSocketConfig`** (`@EnableWebSocketMessageBroker`): STOMP-endpoint `/ws`
  (с поддержкой SockJS для совместимости), простой in-memory брокер на префиксе
  `/topic`, клиентский префикс `/app`. Разрешённые origin — те же dev-хосты
  фронтенда (`5173/3000/4173`).
- **`StompAuthChannelInterceptor`**: при STOMP-кадре `CONNECT` читает заголовок
  `Authorization: Bearer ...`, проверяет JWT тем же `JwtUtil` и привязывает
  `Principal` к сессии. Эндпоинт `/ws/**` добавлен в публичные в `SecurityConfig`
  (авторизация выполняется на уровне STOMP-коннекта, а не HTTP-фильтра).
- **Рассылка сообщений чата**: `ChatServiceImpl.sendMessage(...)` после сохранения
  отправляет DTO в топик `/topic/chats/{chatId}` через `SimpMessagingTemplate`.
  Само сообщение по-прежнему создаётся обычным REST-запросом (он защищён JWT) —
  WebSocket используется только для доставки подписчикам.
- **Рассылка уведомлений**: единый метод `NotificationServiceImpl.create(...)`
  дополнительно публикует уведомление в топик `/topic/users/{userId}`. Поскольку
  этот метод вызывают все модули (бронь, оплата, осмотр, модерация, отзывы),
  **любое** событие становится real-time для адресата независимо от его роли.

Клиент подписывается на `/topic/chats/{chatId}` (открытый диалог) и
`/topic/users/{myUserId}` (личные уведомления/счётчики). Для ручной проверки без
фронтенда в корне репозитория лежит автономная страница `ws-test.html`.

---

## 6. Наполнение тестовыми данными (DataLoader)

**Файл:** `DataLoader` (реализует `CommandLineRunner`, `@Order(1)`).

Вместо Flyway-миграций используется программный сидер (нужно корректное
BCrypt-хеширование паролей). При старте создаются:

- 5 районов Алматы;
- администратор `+77000000000` (`admin = true`);
- 3 арендодателя, 3 арендатора (пароль у всех `password123`);
- 6 квартир с фото, избранное, брони, договоры, чаты, сообщения, платежи, отзывы,
  уведомления;
- вызывается `apartmentSearchService.reindexAll()` для наполнения индекса ES.

**Тестовые учётные записи:**
`admin +77000000000`, `landlord +77011111111`, `tenant +77044444444` — пароль
`password123`.

> Поскольку Hibernate работает в режиме `ddl-auto: update`, при изменении схемы
> (новые колонки) том БД нужно пересоздать: `docker compose -f
> docker-compose.dev.yml down -v && up -d`.

---

## 7. Сводка REST API

| Группа | Базовый путь | Контроллер |
|--------|-------------|------------|
| Аутентификация | `/api/v1/auth` | AuthController |
| Пользователи | `/api/v1/users` | UserController |
| Объявления | `/api/v1/apartments` | ApartmentController |
| Поиск | `/api/v1/apartments/search` | ApartmentController |
| Районы | `/api/v1/districts` | DistrictController |
| Бронирования | `/api/v1/bookings` | BookingController |
| Договоры | `/api/v1/contracts` | ContractController |
| Платежи | `/api/v1/payments` | PaymentController |
| Осмотр | `/api/v1/inspections` | InspectionController |
| Чаты | `/api/v1/chats` | ChatController |
| Уведомления | `/api/v1/notifications` | NotificationController |
| Отзывы | `/api/v1/reviews` | ReviewController |
| Избранное | `/api/v1/favorites` | FavoriteController |
| Жалобы | `/api/v1/reports` | ReportController |
| Файлы | `/api/v1/files` | FileController |
| Арендодатели | `/api/v1/landlords` | LandlordController |
| Админ | `/api/v1/admin` | AdminController |

Полное интерактивное описание доступно через **Swagger UI** (springdoc-openapi)
по адресу `/swagger-ui.html` при запущенном приложении.

---

## 8. Замоканные (имитированные) функциональности

Для дипломной работы важно честно обозначить, что реализовано как имитация:

1. **Платёжный шлюз.** Реального списания денег нет. `payDeposit` / `payRent`
   сразу проставляют статус `PAID` и генерируют псевдо-`transactionId`. Интеграции
   с Kaspi/Halyk/банковским эквайрингом нет — это симуляция успешной оплаты.
2. **Отправка email (OTP).** В режиме разработки письма не уходят реальным
   адресатам — их перехватывает **MailHog** (локальный фейковый SMTP, UI на
   `:8025`). Это сделано сознательно, чтобы не зависеть от внешнего почтового
   сервиса при демонстрации.
3. **ИИ-анализ при недоступности фото.** Если изображение не удалось загрузить,
   Python-сервис возвращает «безопасное» значение `ssimScore = 0.95`
   (трактуется как «повреждений нет»). Это fallback, а не реальный результат.
4. **Эскроу.** «Заморозка» залога — логическая (статусы платежа в БД), реального
   обособленного счёта/эскроу-агента нет.
5. **Геоданные районов.** `safetyScore`, `comfortScore`, `colorCode` районов
   заданы вручную в сидере, а не получены из внешнего источника статистики.

---

## 9. Что предстоит реализовать (Roadmap)

Функции, запланированные, но ещё не реализованные на сервере:

1. **Мультиязычность контента (i18n).** Поддержка трёх языков (казахский,
   русский, английский) для пользовательского контента/уведомлений.
2. **История ИИ-анализов.** Отдельное хранение результатов сравнения с
   возможностью просмотра пар «до/после» и обжалования решения по залогу
   (apelляция).
3. **Кошелёк пользователя.** Баланс, пополнение, возврат залога на кошелёк,
   история транзакций и действий.
4. **Учёт дохода арендодателя.** Агрегация поступлений: аренда, компенсации за
   ущерб, бонусы.
5. **Досрочное расторжение/выселение.** Бизнес-процесс прекращения договора до
   срока с правилами по залогу.
6. **Реальная платёжная интеграция.** Подключение QR Kaspi/Halyk и карточного
   эквайринга вместо текущей симуляции.
7. **ИИ-ассистент.** Подбор квартир с аргументами «за/против», ответы по условиям
   договора, статистический дашборд для арендодателя (на базе LLM-сервиса).
8. **Полноценный модуль апелляций** по решениям модерации и по залогу.

---

## 10. Решённые в ходе разработки проблемы (для раздела «отладка»)

| Проблема | Причина | Решение |
|----------|---------|---------|
| Конфликт репозиториев Spring Data | JPA и Elasticsearch в одном приложении | Явное разделение пакетов: `@EnableJpaRepositories` + `@EnableElasticsearchRepositories` |
| ES возвращал 400 | Несовместимость версий ES 8.x и клиента 9.x | Образ Elasticsearch понижен/согласован до 9.1.0 |
| «id must not be null» при создании квартиры | Фронтенд не присылал `landlordId` | id арендодателя берётся из JWT на сервере |
| Фото не уходили в MinIO | Глобальный `Content-Type: application/json` ломал multipart | Корректная отправка `multipart/form-data` без ручного заголовка |
| ИИ не видел фото из MinIO | `localhost:9000` недоступен внутри контейнера | `rewrite_url()`: `localhost:9000 → minio:9000` |
| OTP не отправлялся | Реальный SMTP отклонял dev-пароль | Переход на MailHog в режиме разработки |

---

*Документ описывает текущее состояние серверной части проекта SafeRent на дату
сборки. Объёмы кода: ~150 Java-классов (сущности, репозитории, сервисы,
контроллеры, DTO, мапперы), отдельный Python-микросервис ИИ.*
