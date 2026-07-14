# Deploy MarcHub Java Backend

## Option 1: Render.com (Recommended - Free)

1. Push this folder to a GitHub repo (e.g., `marchub-java-backend`)
2. Go to https://dashboard.render.com/ → New → Web Service
3. Connect your GitHub repo
4. Use these settings:
   - **Name**: `marchub-backend`
   - **Runtime**: `Docker`
   - **Branch**: `main`
   - **Health Check Path**: `/login` (it will return 404 on GET, but that's fine)
5. Click **Create Web Service**
6. After deploy, copy the URL (e.g., `https://marchub-backend.onrender.com`)

## Option 2: Railway.app (Free)

1. Push to GitHub, connect at https://railway.app/new
2. Railway auto-detects Java + Maven

## Option 3: Build & Run Locally

```bash
cd marchub-backend
./mvnw clean package -DskipTests
java -jar target/marchub-backend-1.0.0.jar
```

## Update Frontend

After deploying, update `const BACKEND` in these files:
- `js/main.js`
- `pages/register.html`
- `pages/enroll.html`

Replace the old Render URL with your new Java backend URL.
