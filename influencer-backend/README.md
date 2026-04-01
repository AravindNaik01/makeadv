# 🚀 Influencer-Business Matching Platform

## 📌 Overview
A full-stack backend system built using Spring Boot and MySQL that connects businesses with influencers. It includes influencer verification, search, ranking, and secure JWT-based authentication.

---

## 🛠 Tech Stack
- Java
- Spring Boot
- MySQL
- JPA (Hibernate)
- JWT Authentication
- Gradle

---

## 🔥 Features

### 👤 Influencer Module
- Add influencer
- Trust score calculation
- Search & filter by category and location
- Ranking system

### 🏢 Business Module
- Businesses can find influencers
- Send collaboration requests

### 🔗 Matching System
- Request lifecycle: PENDING → ACCEPTED
- Stored in database

### 🔐 Authentication & Security
- User registration & login
- JWT token generation
- Secured APIs using token validation

---

## 🔐 API Security
Pass token in header:





--------------------------------------------
#Authorization: Bearer <your_token>

## 🧪 API Endpoints
- `POST /register`
- `POST /login`
- `GET /influencers`
- `POST /request`
- `PUT /accept/{id}`

---

## 💡 Key Learnings
- Built REST APIs using Spring Boot
- Implemented JWT authentication
- Integrated MySQL with JPA
- Designed scalable backend architecture

---

## 🚀 Future Improvements
- React frontend
- Instagram OAuth login
- Deployment (AWS / Render)

---

## 👨‍💻 Author
**Aravind Naik**