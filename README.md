AssetTrack - Enterprise Asset Management System

AssetTrack is a robust, Spring Boot-based backend application designed to streamline the lifecycle of company assets. It provides a multi-role ecosystem for Admins, Asset Managers, and Employees to manage, request, and track hardware or software assets efficiently.

🚀 Live Demo

The application is deployed on Render and can be tested using the Swagger UI:
https://assettrack-63hd.onrender.com/swagger-ui/index.html

✨ Features

🔐 Multi-Role Authorization

Admin: Create new assets and manage all system users.

Asset Manager: Approve or reject asset requests specific to their assigned assets.

Employee: Browse available assets, request items, and return approved assets.

🛠 Core Functionality

JWT Authentication: Secure stateless authentication for all API endpoints.

Request Workflow: Seamless flow from Request (Pending) → Approve/Reject → Return.

Real-time Inventory: Automatic stock adjustment when assets are approved or returned.

Automated Auditing: Created and Modified timestamps for all entities via MongoDB Auditing.

💻 Tech Stack

Framework: Spring Boot 3.4.2

Language: Java 17

Database: MongoDB (Atlas)

Security: Spring Security 6.4.2 + JWT (JJWT)

API Documentation: SpringDoc OpenAPI 2.8.5 (Swagger UI)

Containerization: Docker

Build Tool: Maven

⚙️ Local Setup Instructions

Prerequisites

JDK 17 or higher

MongoDB (Local instance or Atlas Cluster)

Maven (Optional, wrapper included)

Step-by-Step Setup

Clone the Repository

git clone [https://github.com/AbhishekGhume/AssetTrack.git](https://github.com/AbhishekGhume/AssetTrack.git)
cd AssetTrack


Configure Environment Variables
Create an application.properties file in src/main/resources or set the following system variables:

spring.data.mongodb.uri=your_mongodb_connection_string


Build and Run

./mvnw clean install
./mvnw spring-boot:run


Access the API
Navigate to http://localhost:8080/swagger-ui/index.html in your browser.

🌐 Deployment (Render)

This project is configured for easy deployment on Render using Docker.

Dockerfile: Uses a multi-stage build (Maven Build + JRE Runtime).

Environment Variables: Ensure MONGODB_URI is set in the Render dashboard.

SSL Note: For MongoDB Atlas connections, ensure your Atlas IP Whitelist includes 0.0.0.0/0.

📖 API Documentation & Usage

🧪 How to Test with Swagger UI

Register: Use the /public/register endpoint to create a new user.

Login: Use /public/login to receive a JWT token.

Authorize: Click the "Authorize" button at the top of Swagger UI. Enter Bearer YOUR_TOKEN and click Authorize.

Interact: You can now access role-protected endpoints based on your user's role.

Key Endpoints

POST /public/register: User registration.

POST /public/login: Returns a JWT token.

GET /asset/get-all: View all available company assets.

POST /emp/request-asset: Employees can request a specific asset.

PUT /asset-manager/approve/{requestId}: Managers approve pending requests.

POST /admin/create: Admins create new assets.

🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Developed by Abhishek Ghume