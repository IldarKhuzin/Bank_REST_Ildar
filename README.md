📘 Bank Cards Management — Инструкция по запуску (DEV профиль, Docker)

📦 Описание
Это Spring Boot backend-приложение для управления банковскими картами с поддержкой ролей (USER / ADMIN), JWT-аутентификацией, PostgreSQL и Liquibase.

🌐 Стек технологий:
- Java 17+
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 15
- Liquibase
- Docker, Docker Compose
- Swagger / OpenAPI

⚙️ Профили:
- В проекте используется профиль `dev` для разработки (настройки в `application-dev.yml`).
- Он автоматически активируется благодаря настройке `spring.profiles.active=dev` в `application.yml`.

📁 Перед запуском
Убедитесь, что установлены:
- Docker
- Docker Compose

✅ Инструкция по запуску через Docker:

1. Соберите JAR-файл проекта:
   В корне проекта выполните:
   ```
   ./mvnw clean package
   ```
   или
   ```
   mvn clean package
   ```

2. Запустите проект с помощью Docker Compose:
   ```
   docker-compose up --build
   ```

   Это создаст и запустит два сервиса:
    - `bankcards-db` — контейнер с PostgreSQL 15
    - `bankcards-app` — Spring Boot приложение (dev-профиль)

3. Проверьте запуск:
    - API Swagger доступен по адресу: http://localhost:8081/swagger-ui.html
    - База данных PostgreSQL будет доступна на порту 5434.

🔐 Доступ к БД:
- DB Name: `bankcardsdb`
- Username: `postgres`
- Password: `postgres`
- Host: `localhost` (или `bankcards-db` внутри Docker сети)

📂 Конфигурационные файлы:
- `application.yml` — общий конфиг, активирует профиль `dev`
- `application-dev.yml` — dev-конфигурация (Postgres, Liquibase, JPA и т.д.)

🛠️ Полезные команды:
- Остановить контейнеры:
  ```
  docker-compose down
  ```
- Перезапустить с очисткой томов:
  ```
  docker-compose down -v
  ```

---

✍️ Автор: Ильдар  
📅 Последнее обновление: Июнь 2025