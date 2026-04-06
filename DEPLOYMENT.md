# Deployment Guide

This guide explains how to deploy the Influencer Platform to production using Render (backend) and Vercel (frontend).

## Prerequisites

- GitHub account with your code pushed
- Render account (https://render.com)
- Vercel account (https://vercel.com)
- Instagram Meta App credentials (client ID and secret)
- MySQL database (can use Render Database or external provider)

## Backend Deployment (Render)

### 1. Set up a MySQL Database

On Render:
1. Create a new "PostgreSQL" or use an external MySQL database
2. Note the connection URL, username, and password

### 2. Connect Your GitHub Repo to Render

1. Go to https://render.com and sign in
2. Click "New +" > "Web Service"
3. Connect your GitHub repository
4. Select the `influencer-backend` directory as the root
5. Set build command: `./gradlew bootJar`
6. Set start command: `java -jar build/libs/*.jar`

### 3. Add Environment Variables to Render

In Render dashboard, go to your service's "Environment" tab and add:

```
DB_URL=your_mysql_connection_url
DB_USER=your_db_user
DB_PASSWORD=your_db_password
INSTAGRAM_CLIENT_ID=your_instagram_app_id
INSTAGRAM_CLIENT_SECRET=your_instagram_app_secret
INSTAGRAM_REDIRECT_URI=https://your-backend-domain.onrender.com/auth/instagram/callback
APP_FRONTEND_URL=https://your-frontend-domain.vercel.app
ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
APP_PUBLIC_BACKEND_URL=https://your-backend-domain.onrender.com
SPRING_PROFILES_ACTIVE=prod
JPA_SHOW_SQL=false
```

Replace placeholders with your actual values.

### 4. Update Instagram Meta App

In your Meta Developer dashboard:
1. Go to your Instagram app settings
2. Add this redirect URI to "Valid OAuth Redirect URIs":
   ```
   https://your-backend-domain.onrender.com/auth/instagram/callback
   ```

## Frontend Deployment (Vercel)

### 1. Connect Your GitHub Repo to Vercel

1. Go to https://vercel.com and sign in
2. Click "Add New" > "Project"
3. Import your GitHub repository
4. Select the `influencer-frontend` directory as the root

### 2. Add Environment Variables to Vercel

In Vercel project settings, go to "Environment Variables" and add:

```
REACT_APP_API_BASE=https://your-backend-domain.onrender.com
```

### 3. Deploy

Vercel will automatically deploy on every push to `main` branch (or your default branch).

## Verify Production Deployment

1. Visit your frontend at `https://your-frontend-domain.vercel.app`
2. Click "Influencer" > "Continue with Instagram"
3. Verify it redirects to Instagram login (not a dev mock)
4. After login, verify the token is set and you can see the requests dashboard

## Troubleshooting

### CORS Errors
- Ensure `ALLOWED_ORIGINS` environment variable matches your frontend domain
- Make sure the domain includes `https://` and no trailing slash

### Instagram OAuth Redirect Error
- Verify `INSTAGRAM_REDIRECT_URI` matches the one registered in Meta app
- Verify `APP_FRONTEND_URL` is set to your Vercel domain
- Check that `APP_PUBLIC_BACKEND_URL` is the same as the redirect URI base

### Database Connection Failed
- Verify `DB_URL`, `DB_USER`, and `DB_PASSWORD` are correct
- Ensure the database accepts connections from Render
- Check firewall rules if using an external database

## Local Development

Copy `.env.example` to `.env.local` in each folder and fill in local values:

**Backend:**
```bash
cp influencer-backend/.env.example influencer-backend/.env
```

**Frontend:**
```bash
cp influencer-frontend/.env.example influencer-frontend/.env.local
```

Then run locally:
```bash
# Terminal 1: Backend
cd influencer-backend
./gradlew bootRun

# Terminal 2: Frontend
cd influencer-frontend
npm start
```

## Security Notes

- Never commit `.env` files to GitHub
- Use `.env.example` to document required variables
- Store secrets in Render/Vercel environment settings, not in code
- Rotate credentials periodically
