# Shortly — URL Shortener & Click Analytics Backend

A production-deployed **URL Shortener and Click Analytics REST API** built with Java and Spring Boot.

The application generates short URLs using **Base62 encoding**, provides fast redirects using **Caffeine caching**, and asynchronously collects click analytics such as country, browser, device, referrer, and unique visitors.

## 🚀 Live Application

**Frontend:**
https://shortly-url-shortener-vert.vercel.app/

**Backend API:**
https://url-shortener-backend-spe4.onrender.com

## 📂 Repository

**Backend GitHub Repository:**
https://github.com/kalpana23019/UrlShortenerApplication

---

## ✨ Features

### URL Shortening

* Convert long URLs into short URLs.
* Generate deterministic Base62 short codes.
* Database-generated IDs guarantee unique short codes.
* Support custom aliases.
* Detect duplicate aliases.
* Validate HTTP/HTTPS URLs.

### Fast Redirects

* Redirect users from short URLs to original URLs.
* Use **Caffeine Cache** to reduce repeated database lookups.
* Cache hot URLs for faster response times.
* Deactivated and expired URLs are validated before redirecting.

### Click Analytics

Every redirect can generate an asynchronous analytics event containing:

* Click timestamp
* IP address-derived country
* Browser
* Device
* HTTP referrer
* Unique visitor hash

Analytics include:

* Total clicks
* Unique visitors
* Top 5 countries
* Top 5 referrers
* Clicks for the last 7 days

### URL Management

* Custom aliases
* URL expiration
* Link deactivation
* HTTP `403` for deactivated links
* HTTP `410` for expired links
* HTTP `404` for unknown short codes

### QR Code

* Generate a QR code for a short URL using ZXing.

---

## 🛠️ Tech Stack

| Technology      | Purpose                             |
| --------------- | ----------------------------------- |
| Java 21         | Backend programming language        |
| Spring Boot     | REST API and application framework  |
| Spring Data JPA | Database access                     |
| Hibernate       | ORM                                 |
| MySQL           | Relational database                 |
| Caffeine        | Local in-memory caching             |
| Maven           | Build and dependency management     |
| uap-java        | User-Agent/browser/device detection |
| ZXing           | QR code generation                  |
| Docker          | Containerization                    |
| Render          | Backend deployment                  |
| Railway         | MySQL hosting                       |

---

## 🏗️ Architecture

```text
                    Client / React
                         |
                         | HTTP / HTTPS
                         ▼
              ┌─────────────────────┐
              │   Spring Boot API   │
              │       Render        │
              └──────────┬──────────┘
                         |
             ┌───────────┴───────────┐
             │                       │
             ▼                       ▼
      ┌──────────────┐       ┌────────────────┐
      │   Caffeine   │       │     MySQL      │
      │    Cache     │       │    Railway     │
      └──────────────┘       └────────────────┘
             |
             ▼
      Fast URL Redirect
             |
             ▼
      Async Click Analytics
```

---

## 📁 Project Structure

```text
UrlShortenerApplication/
│
├── src/
│   ├── main/
│   │   ├── java/com/app/
│   │   │
│   │   ├── config/
│   │   │   └── CorsConfig.java
│   │   │
│   │   ├── controller/
│   │   │   ├── UrlController.java
│   │   │   ├── AnalyticsController.java
│   │   │   └── QrController.java
│   │   │
│   │   ├── entity/
│   │   │   ├── ShortUrl.java
│   │   │   └── ClickEvent.java
│   │   │
│   │   ├── repository/
│   │   │   ├── ShortUrlRepository.java
│   │   │   └── ClickEventRepository.java
│   │   │
│   │   ├── service/
│   │   │   ├── UrlService.java
│   │   │   ├── ClickService.java
│   │   │   └── AnalyticsService.java
│   │   │
│   │   └── util/
│   │       └── Base62.java
│   │
│   └── resources/
│       └── application.properties
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

# 🔗 REST API

## 1. Create Short URL

### Endpoint

```http
POST /api/urls
```

### Request

```json
{
  "url": "https://www.google.com",
  "alias": null,
  "expiresAt": null
}
```

### Response

```json
{
  "shortUrl": "https://url-shortener-backend-spe4.onrender.com/q0V",
  "code": "q0V"
}
```

### Custom Alias

```json
{
  "url": "https://github.com",
  "alias": "github",
  "expiresAt": null
}
```

If the alias already exists, the API returns:

```http
409 Conflict
```

---

# ↗️ Redirect

### Endpoint

```http
GET /{shortCode}
```

Example:

```http
GET /q0V
```

The server:

1. Checks the Caffeine cache.
2. Loads the URL from MySQL if it is not cached.
3. Checks whether the link is active.
4. Checks whether the link has expired.
5. Records click analytics asynchronously.
6. Redirects the user to the original URL.

---

# 📊 Analytics

### Endpoint

```http
GET /api/urls/{code}/analytics
```

Example:

```http
GET /api/urls/q0V/analytics
```

Example response:

```json
{
  "code": "q0V",
  "totalClicks": 10,
  "uniqueVisitors": 7,
  "topCountries": [
    {
      "name": "India",
      "count": 8
    }
  ],
  "topReferrers": [
    {
      "name": "Direct",
      "count": 6
    }
  ],
  "clicksLast7Days": {
    "2026-09-26": 0,
    "2026-09-27": 1,
    "2026-09-28": 2,
    "2026-09-29": 0,
    "2026-09-30": 3,
    "2026-10-01": 2,
    "2026-10-02": 2
  }
}
```

---

# 🚫 Deactivate URL

### Endpoint

```http
PATCH /api/urls/{code}/deactivate
```

Example:

```http
PATCH /api/urls/q0V/deactivate
```

Response:

```json
{
  "message": "Link deactivated"
}
```

After deactivation, attempting to access the URL returns:

```http
403 Forbidden
```

---

# ⏳ URL Expiration

URLs can optionally have an expiration date and time.

Example:

```json
{
  "url": "https://example.com",
  "alias": null,
  "expiresAt": "2026-12-31T23:59:00"
}
```

Once the expiration time has passed, the short URL returns:

```http
410 Gone
```

---

# 📱 QR Code

The application also provides a QR code endpoint for short URLs.

```http
GET /api/urls/{code}/qr
```

The QR code represents the short URL and can be scanned from a mobile device.

---

# 🔢 Base62 URL Generation

Short codes are generated using **Base62 encoding**.

The character set is:

```text
0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ
```

Examples:

```text
ID     Base62
----------------
0      0
1      1
9      9
10     a
35     z
36     A
61     Z
62     10
100    1c
```

The database generates a unique numeric ID first.

That ID is then converted into Base62.

```text
Database ID
     ↓
   Base62
     ↓
 Short Code
```

This provides deterministic and collision-resistant short codes without repeatedly generating random codes.

---

# ⚡ Caching

Caffeine is used as an in-memory cache for frequently accessed short URLs.

Current configuration:

```text
Maximum entries: 10,000
Expiration:      10 minutes
```

The redirect flow follows a cache-aside approach:

```text
Request
   ↓
Caffeine Cache
   │
   ├── Found → Redirect
   │
   └── Not Found
          ↓
       MySQL
          ↓
        Cache
          ↓
       Redirect
```

This reduces database queries for frequently accessed URLs.

---

# ⚙️ Asynchronous Analytics

Click analytics are recorded asynchronously using Spring's `@Async`.

The redirect does not need to wait for the analytics processing to complete.

```text
User Request
     |
     +-------> Redirect immediately
     |
     +-------> @Async Click Analytics
                    |
                    ├── Country
                    ├── Browser
                    ├── Device
                    ├── Referrer
                    └── Visitor Hash
```

This helps keep the redirect path lightweight.

---

# 🗄️ Database Design

## `short_urls`

```text
short_urls
├── id
├── code
├── long_url
├── expires_at
├── active
└── created_at
```

## `click_events`

```text
click_events
├── id
├── code
├── clicked_at
├── country
├── browser
├── device
├── referrer
└── visitor_hash
```

---

# 💻 Run Locally

## Prerequisites

Install:

* Java 21
* Maven
* MySQL 8+
* Git

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

---

## 1. Clone the Repository

```bash
git clone https://github.com/kalpana23019/UrlShortenerApplication.git
```

Move into the project:

```bash
cd UrlShortenerApplication
```

---

## 2. Configure MySQL

Create a MySQL database:

```sql
CREATE DATABASE shortener;
```

Configure your database connection in `application.properties`.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shortener
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

server.port=8080
```

Do not commit real database passwords to GitHub.

---

## 3. Build the Project

Using Maven:

```bash
mvn clean package
```

Or with Maven Wrapper:

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

---

## 4. Run the Application

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/UrlShortenerApplication-0.0.1-SNAPSHOT.jar
```

The backend will run on:

```text
http://localhost:8080
```

---

# 🐳 Docker

The application can also be built and run using Docker.

Build:

```bash
docker build -t shortly-backend .
```

Run:

```bash
docker run -p 8080:8080 shortly-backend
```

The application uses Java 21 Docker images.

---

# 🌍 Deployment

The backend is deployed using:

```text
Docker
   ↓
Render
   ↓
Spring Boot
```

The MySQL database is hosted on:

```text
Railway
```

Production database configuration uses environment variables rather than hardcoded credentials.

Example:

```properties
spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
spring.datasource.username=${MYSQLUSER}
spring.datasource.password=${MYSQLPASSWORD}

server.port=${PORT:8080}
```

---

# 🔐 Environment Variables

The following environment variables are used in production:

```text
MYSQLHOST
MYSQLPORT
MYSQLDATABASE
MYSQLUSER
MYSQLPASSWORD
PORT
```

Never commit:

```text
.env
application-local.properties
database passwords
API keys
```

to GitHub.

---

# 📌 HTTP Status Codes

| Status    | Meaning                |
| --------- | ---------------------- |
| `200`     | Successful request     |
| `201`     | Short URL created      |
| `301/302` | Redirect               |
| `400`     | Invalid request        |
| `403`     | Link deactivated       |
| `404`     | Short URL not found    |
| `409`     | Alias already exists   |
| `410`     | Link expired           |
| `415`     | Unsupported media type |

---

# 🧠 Key Technical Concepts

This project demonstrates practical experience with:

* REST API development
* Spring Boot
* Spring Data JPA
* Hibernate
* MySQL
* Entity relationships and persistence
* Caching
* Caffeine
* Base62 encoding
* Asynchronous processing
* `@Async`
* HTTP status codes
* Request validation
* IP-based geolocation
* User-Agent parsing
* Analytics aggregation
* Docker
* Environment variables
* Cloud deployment
* CORS
* Git and GitHub

---

# 🔮 Future Improvements

Possible future improvements include:

* Redis distributed caching
* Database migrations using Flyway
* Authentication and user accounts
* Per-user URL management
* Rate limiting
* Improved analytics visualizations
* Advanced geographic analytics
* Custom domains
* Click export to CSV
* Production monitoring and observability
* Distributed asynchronous processing

---

# 👩‍💻 Author

**Kalpana Patil**

Java Full Stack Developer

GitHub:
https://github.com/kalpana23019

LinkedIn:
https://www.linkedin.com/in/kalpana-patil-ak2790/

---

## ⭐ Project

**Shortly — URL Shortener & Click Analytics**

A full-stack URL shortening platform demonstrating Java, Spring Boot, React, MySQL, caching, asynchronous processing, analytics, Docker, and cloud deployment.
