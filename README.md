# Influencer Platform

A full-stack platform connecting businesses with Instagram influencers for sponsored collaborations.

## Tech Stack

- **Backend:** Spring Boot 4.0.4 + Java 17 + MySQL
- **Frontend:** React 19 + React Router + Axios
- **Authentication:** JWT + Instagram OAuth 2.0
- **Deployment:** Render (backend) + Vercel (frontend)

## Features

- **Business Users:** Browse influencers, send collaboration requests
- **Influencers:** Sign in with Instagram, manage incoming requests
- **Instagram OAuth:** Real OAuth 2.0 integration with Meta API
- **JWT Authentication:** Secure token-based auth

## Project Structure

```
MakeAdv/
├── influencer-backend/       # Spring Boot REST API
├── influencer-frontend/      # React web app
├── DEPLOYMENT.md             # Production deployment guide
└── README.md                 # This file
```

## Local Development

### Prerequisites
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Instagram Meta App (for OAuth)

### Setup Backend

1. **Configure Database:**
   ```bash
   cd influencer-backend
   cp .env.example .env
   ```
   Edit `.env` with your MySQL credentials

2. **Start Server:**
   ```bash
   ./gradlew bootRun
   ```
   Backend runs on `http://localhost:8080`

### Setup Frontend

1. **Install Dependencies:**
   ```bash
   cd influencer-frontend
   npm install
   ```

2. **Configure API Base (optional):**
   ```bash
   cp .env.example .env.local
   # Edit .env.local if backend is not on http://localhost:8080
   ```

3. **Start Development Server:**
   ```bash
   npm start
   ```
   Frontend runs on `http://localhost:3000`

## Instagram OAuth Setup

1. Go to [Meta Developer Console](https://developers.facebook.com/apps)
2. Create an app with "Instagram" product
3. Get your **App ID** and **App Secret**
4. Add redirect URI: `http://localhost:8080/auth/instagram/callback`
5. Add credentials to `influencer-backend/.env`:
   ```
   INSTAGRAM_CLIENT_ID=your_app_id
   INSTAGRAM_CLIENT_SECRET=your_app_secret
   ```

## API Endpoints

### Auth
- `POST /register` - Register a business user
- `POST /login` - Login (business only)
- `GET /auth/instagram/url` - Get Instagram OAuth URL
- `GET /auth/instagram/callback` - Instagram OAuth callback

### Influencers
- `GET /influencers` - List all influencers
- `POST /request` - Send collaboration request
- `GET /requests/influencer` - Get pending requests
- `PUT /accept/{id}` - Accept request
- `PUT /reject/{id}` - Reject request

## Production Deployment

See [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed instructions on:
- Deploying backend to Render
- Deploying frontend to Vercel
- Configuring production environment variables

## Environment Variables

### Backend
- `DB_URL` - MySQL connection string
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password
- `INSTAGRAM_CLIENT_ID` - Meta app ID
- `INSTAGRAM_CLIENT_SECRET` - Meta app secret
- `APP_FRONTEND_URL` - Frontend URL
- `ALLOWED_ORIGINS` - CORS allowed origins

### Frontend
- `REACT_APP_API_BASE` - Backend API URL (default: http://localhost:8080)

## License

Private project

## Support

For issues or questions, refer to the DEPLOYMENT.md guide.
