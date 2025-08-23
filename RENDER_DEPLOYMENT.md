# Render Deployment Guide for Spring Boot Backend

## 🚀 Quick Start

1. **Go to [Render.com](https://render.com)**
2. **Sign in with your GitHub account**
3. **Click "New +" → "Web Service"**
4. **Connect your repository**: `AlphaDoc1/noteBackend`
5. **Deploy!**

## 📋 Configuration Details

### Service Type: Web Service
- **Environment**: Java
- **Plan**: Free (or upgrade as needed)
- **Build Command**: `mvn clean install -DskipTests`
- **Start Command**: `java -jar target/notes-management-system-0.0.1-SNAPSHOT.jar`
- **Health Check**: `/actuator/health`

### Environment Variables to Set

#### Required Variables:
```
PORT=8080
AWS_S3_BUCKET=studentnotes-bucket
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
MONGODB_URI=your_mongodb_connection_string
MONGODB_DATABASE=notesdb
```

#### Optional Variables:
```
JAVA_OPTS=-Xmx512m -Xms256m
SPRING_PROFILES_ACTIVE=production
```

## 🔧 Render Dashboard Setup

### 1. Connect Repository
- Choose "Connect a repository"
- Select `AlphaDoc1/noteBackend`
- Render will auto-detect it's a Java project

### 2. Configure Service
- **Name**: `notes-backend` (or your preferred name)
- **Environment**: `Java`
- **Region**: Choose closest to your users
- **Branch**: `main`
- **Build Command**: `mvn clean install -DskipTests`
- **Start Command**: `java -jar target/notes-management-system-0.0.1-SNAPSHOT.jar`

### 3. Set Environment Variables
- Go to **Environment** tab
- Add each environment variable listed above
- **Important**: Never commit real credentials to git!

### 4. Deploy
- Click **Create Web Service**
- Render will automatically build and deploy
- Monitor the build logs for any issues

## 📊 Monitoring & Health Checks

### Health Check Endpoint
- **URL**: `https://your-app.onrender.com/actuator/health`
- **Expected Response**: `{"status":"UP"}`
- Render will use this to monitor your service

### Logs
- View real-time logs in Render dashboard
- Monitor resource usage
- Check for any errors or warnings

## 🔄 Auto-Deploy

- **Automatic**: Every push to `main` branch triggers deployment
- **Manual**: You can manually deploy from any branch
- **Rollback**: Easy rollback to previous versions

## 💰 Free Tier Limits

- **Build Time**: 500 minutes/month
- **Runtime**: 750 hours/month
- **Bandwidth**: 100 GB/month
- **Sleep**: Free tier services sleep after 15 minutes of inactivity

## 🚨 Troubleshooting

### Common Issues:

1. **Build Fails**
   - Check Maven dependencies
   - Verify Java version compatibility
   - Check build logs for specific errors

2. **Service Won't Start**
   - Verify environment variables are set
   - Check if port 8080 is available
   - Review startup logs

3. **Health Check Fails**
   - Ensure Spring Boot Actuator is included
   - Check if `/actuator/health` endpoint is accessible
   - Verify application.properties configuration

### Useful Commands:
```bash
# Check build status
curl https://your-app.onrender.com/actuator/health

# View logs in Render dashboard
# Go to your service → Logs tab
```

## 🌐 Custom Domain (Optional)

1. Go to your service settings
2. Click **Custom Domains**
3. Add your domain
4. Update DNS records as instructed

## 📈 Scaling

- **Free**: 1 instance
- **Paid Plans**: Multiple instances, auto-scaling
- **Database**: Add Render Postgres if needed

## 🔐 Security Best Practices

- ✅ Environment variables for secrets
- ✅ No hardcoded credentials
- ✅ HTTPS enabled by default
- ✅ Regular security updates

## 📞 Support

- **Documentation**: [docs.render.com](https://docs.render.com)
- **Community**: [community.render.com](https://community.render.com)
- **Status**: [status.render.com](https://status.render.com)

---

**Your backend will be live at**: `https://your-app-name.onrender.com`
