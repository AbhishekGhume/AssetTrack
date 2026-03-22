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
[![AWS S3](https://img.shields.io/badge/AWS-S3%20Storage-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)](https://aws.amazon.com/s3/)
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

**AssetTrack** is a production-ready Spring Boot backend application that streamlines the entire lifecycle of company assets — from creation to assignment, approval, and return. Built with a clean multi-role ecosystem, it ensures every action is authorized, audited, and traceable. Assets support image uploads stored on AWS S3.

---

## ✨ Features

### 🔐 Multi-Role Authorization
| Role | Capabilities |
|------|-------------|
| **Admin** | Create assets (with image), assign them to managers, manage user roles, view all system users |
| **Asset Manager** | Approve or reject requests for their assigned assets |
| **Employee** | Browse assets, submit requests, return approved items |

### ⚙️ Core Functionality

```
📦 Asset Created by Admin (with optional image)
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
- **🖼️ Image Upload** — Asset images uploaded and stored on AWS S3
- **📊 Real-time Inventory** — Stock auto-adjusts on approval and return
- **🕐 Automated Auditing** — `createdAt` & `updatedAt` timestamps via MongoDB Auditing
- **🛡️ Request Integrity** — Prevents duplicate pending requests for the same asset
- **👑 Role Management** — Admin can promote or demote users to/from Admin role
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
| **File Storage** | AWS S3 (SDK v2) |
| **API Docs** | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| **Containerization** | Docker (Multi-stage build) |
| **Build Tool** | Maven (wrapper included) |

</div>

---

## 🏗️ Architecture & Roles

```
┌──────────────────────────────────────────────────────────┐
│                      AssetTrack API                      │
├──────────────┬───────────────────┬───────────────────────┤
│   /public    │      /admin       │   /asset-manager      │
│  (No Auth)   │   (ADMIN role)    │  (ASSET_MGR role)     │
│              │                   │                       │
│  • Register  │  • Create Asset   │  • View Requests      │
│  • Login     │  • View Users     │  • Approve/Reject     │
│              │  • Make Admin     │                       │
│              │  • Remove Admin   │                       │
├──────────────┴───────────────────┴───────────────────────┤
│                /emp  (EMPLOYEE role)                     │
│      • Request Asset  • Return Asset  • View Requests    │
└──────────────────────────────────────────────────────────┘
```

---

## 📖 API Endpoints

<details>
<summary><b>🔓 Public Endpoints</b> (No authentication required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/public/register` | Register a new user |
| `POST` | `/public/login` | Login and receive JWT token |

> ⚠️ **Note:** The `role` field must be a proper JSON array, e.g. `["EMPLOYEE"]`. Registering with `ADMIN` role is not allowed — use the admin promotion endpoint instead.

</details>

<details>
<summary><b>👁️ Asset Endpoints</b> (Any authenticated user)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/asset/get-all` | View all available assets (includes image URL) |

</details>

<details>
<summary><b>👔 Admin Endpoints</b> (ADMIN role required)</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/create` | Create a new asset with optional image upload |
| `GET` | `/admin/get-all` | View all registered users |
| `PUT` | `/admin/make-admin/{id}` | Promote a user to Admin role |
| `PUT` | `/admin/remove-admin/{id}` | Remove Admin role from a user |

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
| `POST` | `/emp/request-asset/{assetId}` | Request an asset |
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
- ✅ AWS account with an S3 bucket
- ✅ Maven *(optional — wrapper included)*

### Step-by-Step

**1. Clone the repository**
```bash
git clone https://github.com/AbhishekGhume/AssetTrack.git
cd AssetTrack
```

**2. Configure environment variables**

Create a `.env` file in the project root:
```properties
MONGODB_URI=your_mongodb_connection_string
ACCESS_KEY=your_aws_access_key
SECRET_KEY=your_aws_secret_key
REGION=ap-south-1
BUCKET_NAME=your_s3_bucket_name
```

**3. Build and run**
```bash
# Using Maven wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# Or using Docker
docker build -t assettrack .
docker run \
  -e MONGODB_URI=your_uri \
  -e ACCESS_KEY=your_key \
  -e SECRET_KEY=your_secret \
  -e REGION=ap-south-1 \
  -e BUCKET_NAME=your_bucket \
  -p 8080:8080 assettrack
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
| `ACCESS_KEY` | Your AWS IAM access key |
| `SECRET_KEY` | Your AWS IAM secret key |
| `REGION` | Your S3 bucket region (e.g. `ap-south-1`) |
| `BUCKET_NAME` | Your S3 bucket name |
| `PORT` | `8080` |

> 💡 **MongoDB Atlas Tip:** Whitelist IP `0.0.0.0/0` in Atlas Network Access to allow connections from Render's dynamic IPs.

> 💡 **AWS S3 Tip:** Make sure your S3 bucket has public read access enabled so that asset image URLs are accessible. Add a bucket policy allowing `s3:GetObject` for `Principal: *`.

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
> ⚠️ The `role` field must be a **JSON array**, not a string. Valid values: `EMPLOYEE`, `ASSET_MANAGER`. The `ADMIN` role can only be assigned via `/admin/make-admin/{id}`.

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
