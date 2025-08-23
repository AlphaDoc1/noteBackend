# Railway Deployment Guide

## Prerequisites
- Railway account
- GitHub repository connected to Railway
- Environment variables configured

## Deployment Steps

### 1. Connect to Railway
1. Go to [Railway.app](https://railway.app)
2. Sign in with your GitHub account
3. Click "New Project"
4. Select "Deploy from GitHub repo"
5. Choose your repository: `AlphaDoc1/noteBackend`

### 2. Configure Environment Variables
In Railway dashboard, add these environment variables:

```
PORT=8080
AWS_S3_BUCKET=studentnotes-bucket
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
MONGODB_URI=your_mongodb_connection_string
MONGODB_DATABASE=notesdb
```

### 3. Deploy
1. Railway will automatically detect the Java project
2. It will use the `nixpacks.toml` configuration
3. Build process: `mvn clean install -DskipTests`
4. Start command: `java -jar target/notes-management-system-0.0.1-SNAPSHOT.jar`

### 4. Health Check
- Railway will check: `/actuator/health`
- Make sure your app responds to this endpoint

### 5. Custom Domain (Optional)
- In Railway dashboard, go to Settings
- Add your custom domain
- Update DNS records as instructed

## Configuration Files Added

- `railway.json` - Railway deployment configuration
- `nixpacks.toml` - Build and runtime configuration
- `Procfile` - Alternative deployment method
- `system.properties` - Java version specification
- `.railwayignore` - Files to exclude from deployment
- Updated `application.properties` - Environment variable support
- Added Spring Boot Actuator dependency

## Troubleshooting

1. **Build fails**: Check Java version compatibility
2. **Port issues**: Ensure `PORT` environment variable is set
3. **Database connection**: Verify MongoDB URI is correct
4. **AWS credentials**: Ensure AWS environment variables are set

## Monitoring
- Railway provides built-in logging
- Check `/actuator/health` for application health
- Monitor resource usage in Railway dashboard
