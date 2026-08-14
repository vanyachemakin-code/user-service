# UserApp

Микросервисная система на Spring Boot / Spring Cloud, реализующая управление пользователями с асинхронными уведомлениями по email через Kafka. Проект демонстрирует ключевые паттерны микросервисной архитектуры: **Service Discovery**, **External Configuration**, **API Gateway** и **Circuit Breaker**.

## Содержание

- [Архитектура](#архитектура)
- [Модули проекта](#модули-проекта)
- [Технологический стек](#технологический-стек)
- [Реализованные паттерны](#реализованные-паттерны)
- [API](#api)
- [Запуск локально](#запуск-локально)
- [Запуск в Docker](#запуск-в-docker)
- [Тестирование](#тестирование)
- [Структура конфигурации](#структура-конфигурации)

## Архитектура

```
                        ┌─────────────────────┐
                        │   Discovery Server  │
                        │      (Eureka)       │
                        └──────────▲──────────┘
                                   │ регистрация
                ┌──────────────────┼──────────────────┐
                │                  │                  │
      ┌─────────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐
      │  Gateway Service │ │  User Service  │ │ Notification   │
      │  (Spring Cloud   │ │                │ │ Service        │
      │   Gateway)       │ │                │ │                │
      └─────────┬────────┘ └─────────┬──────┘ └──────────┬─────┘
                │                    │                   │
        клиентский трафик        PostgreSQL         Kafka Consumer
                                     │                   │
                              ┌──────▼──────┐    ┌───────▼───────┐
                              │  Kafka      │◄───┤  SMTP (mail)  │
                              │  Producer   │    └───────────────┘
                              └─────────────┘

      ┌────────────────────┐
      │   Config Server    │◄── все сервисы получают конфигурацию отсюда
      │  (Spring Cloud     │
      │   Config, native)  │
      └────────────────────┘
```

Клиент обращается к системе через **Gateway** (`localhost:8765`), который находит нужный сервис через **Eureka** и проксирует запрос. `user-service` при создании/удалении пользователя публикует внутреннее событие, которое **после коммита транзакции** асинхронно отправляется в Kafka. `notification-service` слушает топик и отправляет email-уведомление.

## Модули проекта

| Модуль | Назначение | Порт |
|---|---|---|
| `common-dto` | Общие DTO для обмена сообщениями между сервисами (Kafka-события) | — |
| `discovery-server` | Service Discovery на базе Netflix Eureka | 8761 |
| `config-server` | Централизованная конфигурация (Spring Cloud Config, native-профиль) | 8888 |
| `gateway-service` | API Gateway (Spring Cloud Gateway), маршрутизация через Eureka | 8765 |
| `user-service` | CRUD пользователей, публикация событий в Kafka | 8080 |
| `notification-service` | Consumer Kafka-событий, отправка email-уведомлений | 8082 |

## Технологический стек

- **Java 17**, Spring Boot 3.2.5, Spring Cloud 2023.0.1
- **Spring Data JPA** + PostgreSQL
- **Spring Kafka** — асинхронный обмен событиями
- **Spring HATEOAS** — навигационные ссылки в ответах API
- **Springdoc OpenAPI** — Swagger-документация
- **Resilience4j** — Circuit Breaker
- **Spring Cloud Netflix Eureka** — Service Discovery
- **Spring Cloud Config** — External Configuration
- **Spring Cloud Gateway** — API Gateway
- **Testcontainers** (Kafka, PostgreSQL), **GreenMail**, **Mockito**, **AssertJ** — тестирование
- **Docker / Docker Compose** — контейнеризация и оркестрация
- **Mailhog** — тестовый SMTP-сервер для Docker-окружения

## Реализованные паттерны

### 1. Service Discovery
`discovery-server` поднимает Eureka Server. `user-service`, `notification-service` и `gateway-service` регистрируются в нём при старте и обнаруживают друг друга по имени приложения, а не по фиксированному адресу.

### 2. External Configuration
`config-server` в native-режиме раздаёт конфигурацию из `config-repo/`. Каждый бизнес-сервис хранит только своё имя и адрес Config Server локально — вся остальная конфигурация (datasource, Kafka, Eureka, Circuit Breaker) приходит централизованно.

### 3. API Gateway
`gateway-service` маршрутизирует запросы через `lb://<service-name>` — реальный адрес инстанса определяется динамически через Eureka, а не хардкодится. Поддерживает `X-Forwarded-*` заголовки (`server.forward-headers-strategy=framework`), поэтому HATEOAS-ссылки в ответах корректно отражают публичный адрес Gateway, а не внутренний адрес backend-сервиса.

### 4. Circuit Breaker
Публикация Kafka-события вынесена в `UserNotificationProducer` и защищена Resilience4j:

- срабатывает **после коммита транзакции** (`@TransactionalEventListener(phase = AFTER_COMMIT)`) — если сохранение пользователя в БД откатится, событие в Kafka не уйдёт;
- выполняется асинхронно (`@Async`) — отправка не блокирует HTTP-ответ клиенту;
- при систематической недоступности Kafka `@CircuitBreaker` переключается на `fallback`-метод, логирующий проблему, вместо падения запроса.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      kafkaProducer:
        sliding-window-size: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

### Дополнительно: Swagger + HATEOAS
`user-service` документирован через Springdoc OpenAPI (`/swagger-ui.html`) и возвращает ответы в формате HAL с навигационными ссылками (`self`, `all-users`, `update`, `delete`).

## API

Базовый путь: `/api/v1/user-service/user` (через Gateway: `http://localhost:8765/api/v1/user-service/user`)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/add` | Создать пользователя |
| `GET` | `/{id}` | Получить пользователя по ID |
| `GET` | `/list` | Получить список пользователей |
| `PUT` | `/{id}/update` | Обновить пользователя |
| `DELETE` | `/{id}/delete` | Удалить пользователя |

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Запуск локально

### Предварительные требования
- JDK 17
- Maven
- Docker (для Testcontainers и реальных Postgres/Kafka)

### Порядок запуска

Локальный конфиг каждого сервиса указывает на Config Server (`http://localhost:8888`), поэтому порядок важен:

```bash
# 1. Discovery Server
mvn spring-boot:run -pl discovery-server

# 2. Config Server (после того как Discovery поднялся)
mvn spring-boot:run -pl config-server

# 3. Postgres и Kafka (реальные, не Testcontainers)
docker run --name user-service-postgres \
  -e POSTGRES_USER=<ваш логин> -e POSTGRES_PASSWORD=<ваш пароль> \
  -e POSTGRES_DB=<название базы> -p 5432:5432 -d postgres:15-alpine

# 4. User Service и Notification Service
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl notification-service

# 5. Gateway Service
mvn spring-boot:run -pl gateway-service
```

### Проверка

```bash
# Все сервисы зарегистрированы в Eureka
open http://localhost:8761

# Конфигурация реально раздаётся
curl http://localhost:8888/user-service/default

# Запрос через Gateway
curl http://localhost:8765/api/v1/user-service/user/list
```

## Запуск в Docker

### Подготовка

Создайте файл `.env` в корне проекта.
Используйте `.env.example` как шаблон и подставьте свои значения:

```env
POSTGRES_USER=<ваш логин>
POSTGRES_PASSWORD=<ваш пароль>
POSTGRES_DB=<название базы>

MAIL_USERNAME=<логин SMTP>
MAIL_PASSWORD=<пароль SMTP>
```

### Сборка и запуск

```bash
mvn clean package -DskipTests
docker compose up --build
```

### Проверка

```bash
# Eureka — три бизнес-сервиса в статусе UP
open http://localhost:8761

# Создать пользователя через Gateway
curl -X POST http://localhost:8765/api/v1/user-service/user/add 
  -H "Content-Type: application/json" 
  -d '{"name":"Test","email":"test@docker.com","age":30}'

# Проверить, что письмо дошло (веб-интерфейс Mailhog)
open http://localhost:8025
```

Если письмо появилось в Mailhog — подтверждена полная цепочка: Gateway → Eureka → user-service → PostgreSQL → Kafka → notification-service → SMTP.

### Особенности Docker-окружения

Для контейнерной среды используется отдельный профиль `docker`, подмешивающий адреса сервисов по именам контейнеров (`postgres`, `kafka`, `discovery-server`) вместо `localhost`. Профильные файлы (`user-service-docker.yml`, `notification-service-docker.yml`) лежат в `config-repo/docker/` и активируются через `SPRING_PROFILES_ACTIVE=docker` в `docker-compose.yml`.

## Тестирование

```bash
# Все тесты user-service (Testcontainers поднимет Kafka и Postgres)
mvn test -pl user-service -am

# Все тесты notification-service (Testcontainers + GreenMail)
mvn test -pl notification-service -am
```

Тесты полностью изолированы от внешнего окружения — не требуют запущенного Config Server или реальных Kafka/Postgres. Ключевые тестовые сценарии:

- CRUD-операции пользователя (`TestUserController`, `TestUserService`)
- Публикация событий и обработка ошибок Kafka на уровне юнит-тестов (`TestUserNotificationProducer`)
- Интеграционный тест реального срабатывания Circuit Breaker и fallback через поднятый Spring-контекст (`IntegrationTestUserNotificationProducer`)
- Consumer-сторона: получение Kafka-события и отправка email (`TestNotificationController`, с проверкой через GreenMail)

## Структура конфигурации

```
config-server/src/main/resources/
├── application.yml              # настройки самого Config Server
└── config-repo/
    ├── user-service.yml         # конфигурация для локального запуска
    ├── notification-service.yml
    └── docker/
        ├── user-service-docker.yml       # переопределения для Docker-сети
        └── notification-service-docker.yml
```

Каждый бизнес-сервис хранит в своём `application.yml` только:

```yaml
spring:
  application:
    name: user-service
  config:
    import: optional:configserver:http://localhost:8888
```

Все остальные настройки (datasource, Kafka, Eureka, Circuit Breaker, mail) приходят с Config Server.
