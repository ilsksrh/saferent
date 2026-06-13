# SafeRent — Roadmap доработок (согласовано с заказчиком)

Контекст: после базового MVP нужно доработать бизнес-логику (особенно платежи и
эскроу/осмотр) и UX. Ниже — согласованный план. ИИ-ассистент «Жанибек» **отложен**.

## Принятые решения
- **Осмотр квартиры:** лэндлорд грузит фото «до» → тенант подтверждает (дедлайн) →
  тенант грузит фото «после» (дедлайн) → ИИ-вердикт приходит обоим в реал-тайм →
  **решение о возврате залога принимает только админ**.
- **Карта:** Google Maps (ключ во фронтовом `.env`, не в коде/гите). Координаты
  квартиры лэндлорд ставит **пином на карте** при создании объявления.
- **3D-тур:** 360°-панорама через Pannellum (open-source); лэндлорд грузит 360-фото.
- **Карта оплаты:** хранить **только последние 4 цифры + срок** (CVV/полный номер
  не хранятся). Реального списания нет — это мок.
- **Жанибек (ИИ-чат):** отложен.

> ⚠️ Google Maps ключ был прислан в чат открытым текстом — ограничить его в Google
> Cloud Console (HTTP referrers + только Maps JS/Geocoding) и желательно ротировать.

---

## Фаза 1 — Платежи  *(в работе)*
**Backend:**
- `RentPeriod` (сущность) + `RentPeriodStatus` (DUE/PAID/OVERDUE) + repo. Генерация
  помесячного графика при активации договора (`ContractServiceImpl.sign`).
- Фикс `PaymentServiceImpl.payRent`: оплачивается **ближайший неоплаченный период**
  (нельзя платить неограниченно/не по порядку); сумма = `rentAmount`.
- `GET /payments/schedule/{contractId}` — календарь платежей.
- `@EnableScheduling` + `RentReminderScheduler`: напоминание за 5 дней до `dueDate`,
  пометка просроченных (OVERDUE).

**Frontend:**
- PaymentPage: убрать Halyk (оставить Kaspi + Card). Kaspi → QR-код (`qrcode.react`),
  Card → форма (номер/MM/YY/CVV) + чекбокс «сохранить по умолчанию», список
  сохранённых карт. Календарь платежей с бейджами DUE/PAID/OVERDUE.
- Backend: `SavedCard` (user, last4, expMonth, expYear, isDefault) + CRUD; в payRent
  можно указать сохранённую карту.

## Фаза 2 — Осмотр / эскроу
- `uploadCheckinPhoto` → только **лэндлорд**; новый `confirmCheckin` (тенант, дедлайн);
  `uploadCheckoutPhoto` → тенант (дедлайн). Дедлайны на `Contract`.
- Вердикт ИИ сохраняется (поля на Contract: avgSsim/verdict/decidedAt) и шлётся
  **обоим в реал-тайм** (через существующий WebSocket-брод­каст уведомлений).
- `release/tenant` и `release/landlord` → **только ADMIN** (`@PreAuthorize`), убрать
  кнопки у тенанта/лэндлорда. Очередь решений в админке: `GET /admin/escrow/pending`.

## Фаза 3 — Профиль + чат
- Профиль тенанта: текущая снимаемая квартира (из активного договора) + баланс
  (депозит в эскроу + сумма неоплаченной аренды).
- Чат: поле поиска лэндлорда по ФИО (`GET /landlords/search?name=`) → старт чата.
  Для прямого чата без объявления — сделать `Chat.apartment` nullable.

## Фаза 4 — UI деталей (+ карты, рейтинги, хост, 3D, i18n)
- **Клик по карточке** на главной → сразу в детали (отключить лайтбокс в карточке;
  оставить зум только на странице деталей).
- **Рейтинги-разбивка** (Airbnb-стиль): расширить `Review` 6 категориями
  (cleanliness/accuracy/check-in/communication/location/value) + распределение 5–1
  звёзд. `GET /reviews/breakdown/{userId}`.
- **Google-карта** с меткой в деталях + пин-пикер в создании объявления
  (`@react-google-maps/api`, `lat`/`lng` на `Apartment`).
- **Секция «Meet your host»**: производные метрики (месяцев хостинга из createdAt,
  кол-во отзывов, рейтинг, Superhost = рейтинг≥4.8 и N+ отзывов) + поля User
  (languages, city, bio).
- **«Things to know»**: поля Apartment (checkInTime, checkOutTime, maxGuests,
  cancellationPolicy, houseRules, safety-флаги) с дефолтами.
- **3D-тур**: 360-фото в MinIO + Pannellum-вьювер (кнопка в деталях).
- **i18n**: `react-i18next` + локали `en/ru/kk` + переключатель языка.

---

## Проверка (по фазам)
Запуск: `docker compose -f docker-compose.dev.yml up -d` + `cd D:\saferent-web && npm run dev`.
Тест-аккаунты (`password123`): admin `+77000000000`, landlord `+77011111111`,
tenant `+77044444444`. Юнит-тесты бэкенда: `./mvnw test`.
