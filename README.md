<div align="center">

<img src="https://img.shields.io/badge/AssetTrack-Enterprise%20Asset%20Management-6366f1?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0xMiAyQzYuNDggMiAyIDYuNDggMiAxMnM0LjQ4IDEwIDEwIDEwIDEwLTQuNDggMTAtMTBTMTcuNTIgMiAxMiAyem0tMSAxNHYtNEg3bDUtOHY0aDRsLTUgOHoiLz48L3N2Zz4=" />

# AssetTrack

### Enterprise Asset Management System

*A robust, multi-role backend for managing the complete lifecycle of company assets*

<br/>

[![Live Demo](https://img.shields.io/badge/🚀%20Live%20Demo-Swagger%20UI-22c55e?style=for-the-badge)](https://assettrack-63hd.onrender.com/swagger-ui/index.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/atlas)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

<br/>

> **Test it live →** [`https://assettrack-63hd.onrender.com/swagger-ui/index.html`](https://assettrack-63hd.onrender.com/swagger-ui/index.html)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture & Roles](#-architecture--roles)
- [API Endpoints](#-api-endpoints)
- [Local Setup](#-local-setup)
- [Deployment](#-deployment-on-render)
- [How to Test](#-how-to-test-with-swagger-ui)

---

## 🌟 Overview

**AssetTrack** is a production-ready Spring Boot backend application that streamlines the entire lifecycle of company assets — from creation to assignment, approval, and return. Built with a clean multi-role ecosystem, it ensures every action is authorized, audited, and traceable.

---

## ✨ Features

### 🔐 Multi-Role Authorization
| Role | Capabilities |
|------|-------------|
| **Admin** | Create assets, assign them to managers, view all system users |
| **Asset Manager** | Approve or reject requests for their assigned assets |
| **Employee** | Browse assets, submit requests, return approved items |

### ⚙️ Core Functionality

```
📦 Asset Created by Admin
        │
        ▼
👤 Employee Requests Asset  →  Status: PENDING
        │
        ▼
✅ Manager Approves / ❌ Rejects  →  Status: APPROVED / REJECTED
        │
        ▼
🔄 Employee Returns Asset  →  Status: RETURNED
```

- **🔑 JWT Authentication** — Stateless, secure token-based auth for every endpoint
- **📊 Real-time Inventory** — Stock auto-adjusts on approval and return
- **🕐 Automated Auditing** — `createdAt` & `updatedAt` timestamps via MongoDB Auditing
- **🛡️ Request Integrity** — Prevents duplicate pending requests for the same asset
- **⚡ Exception Handling** — Global handler with meaningful HTTP status codes

---

## 💻 Tech Stack

<div align="center">

| Layer | Technology |
|-------|-----------|
| **Framework** | Spring Boot 3.4.2 |
| **Language** | Java 17 |
| **Database** | MongoDB Atlas |
| **Security** | Spring Security 6 + JWT (JJWT 0.12.5) |
| **API Docs** | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| **Containerization** | Docker (Multi-stage build) |
| **Build Tool** | Maven (wrapper included) |

</div>

---

## 🏗️ Architecture & Roles

```
┌─────────────────────────────────────────────────────┐
│                    AssetTrack API                    │
├──────────────┬──────────────────┬───────────────────┤
│   /public    │     /admin       │  /asset-manager   │
│  (No Auth)   │  (ADMIN role)    │ (ASSET_MGR role)  │
│              │                  │                   │
│  • Register  │  • Create Asset  │  • View Requests  │
│  • Login     │  • View Users    │  • Approve/Reject │
├──────────────┴──────────────────┴───────────────────┤
│              /emp  (EMPLOYEE role)                   │
│   • Request Asset  • Return Asset  • View Requests  │
└─────────────────────────────────────────────────────┘
```

---

## 📖 API Endpoints

<details>
<summary><b>🔓 Public Endpoints</b> (No authentication required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/public/register` | Register a new user |
| `POST` | `/public/login` | Login and receive JWT token |

</details>

<details>
<summary><b>👁️ Asset Endpoints</b> (Any authenticated user)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/asset/get-all` | View all available assets |

</details>

<details>
<summary><b>👔 Admin Endpoints</b> (ADMIN role required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/create` | Create a new asset |
| `GET` | `/admin/get-all` | View all registered users |

</details>

<details>
<summary><b>🗂️ Asset Manager Endpoints</b> (ASSET_MANAGER role required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/asset-manager/get-all-req` | View all assigned requests |
| `GET` | `/asset-manager/get-all-pending` | Filter: pending requests |
| `GET` | `/asset-manager/get-all-approved` | Filter: approved requests |
| `GET` | `/asset-manager/get-all-rejected` | Filter: rejected requests |
| `GET` | `/asset-manager/get-all-returned` | Filter: returned requests |
| `PUT` | `/asset-manager/approve/{requestId}` | Approve a request |
| `PUT` | `/asset-manager/reject/{requestId}` | Reject a request |

</details>

<details>
<summary><b>👤 Employee Endpoints</b> (EMPLOYEE role required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/emp/get-all-req` | View all my requests |
| `POST` | `/emp/request-asset` | Request an asset |
| `PUT` | `/emp/return/{requestId}` | Return an approved asset |
| `GET` | `/emp/get-all-pending` | Filter: my pending requests |
| `GET` | `/emp/get-all-approved` | Filter: my approved requests |
| `GET` | `/emp/get-all-rejected` | Filter: my rejected requests |
| `GET` | `/emp/get-all-returned` | Filter: my returned requests |

</details>

---

## ⚙️ Local Setup

### Prerequisites

- ✅ JDK 17 or higher
- ✅ MongoDB (local instance or Atlas cluster)
- ✅ Maven *(optional — wrapper included)*

### Step-by-Step

**1. Clone the repository**
```bash
git clone https://github.com/AbhishekGhume/AssetTrack.git
cd AssetTrack
```

**2. Configure environment variables**

Create `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=your_mongodb_connection_string
server.port=8080
```

**3. Build and run**
```bash
# Using Maven wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# Or using Docker
docker build -t assettrack .
docker run -e MONGODB_URI=your_uri -p 8080:8080 assettrack
```

**4. Access the API**

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🌐 Deployment on Render

This project is Docker-ready for one-click deployment on Render.

**Required Environment Variables on Render:**

| Variable | Value |
|----------|-------|
| `MONGODB_URI` | Your MongoDB Atlas connection string |
| `PORT` | `8080` |

> 💡 **MongoDB Atlas Tip:** Whitelist IP `0.0.0.0/0` in Atlas Network Access to allow connections from Render's dynamic IPs.

---

## 🧪 How to Test with Swagger UI

> **Live URL:** [https://assettrack-63hd.onrender.com/swagger-ui/index.html](https://assettrack-63hd.onrender.com/swagger-ui/index.html)

**Step 1 — Register a user**
```json
POST /public/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": ["EMPLOYEE"]
}
```

**Step 2 — Login to get your token**
```json
POST /public/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Step 3 — Authorize in Swagger**

Click the **🔓 Authorize** button → Enter `Bearer YOUR_TOKEN_HERE` → Click **Authorize**

**Step 4 — Explore!**

You can now call all role-protected endpoints based on your assigned role.


---

<div align="center">

**Built with ❤️ by [Abhishek Ghume](https://github.com/AbhishekGhume)**

⭐ If you found this project useful, please give it a star!

</div>
