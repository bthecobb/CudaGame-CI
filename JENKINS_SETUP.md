# Jenkins Setup Guide for CudaGame-CI

## 📥 Step 1: Install Jenkins on Windows

### Option A: Using Windows Installer (Recommended)
1. Download Jenkins for Windows from: https://www.jenkins.io/download/
2. Choose "Windows" → Download the `.msi` installer
3. Run the installer and follow the wizard
4. Jenkins will be installed as a Windows service

### Option B: Using WAR file
1. Download `jenkins.war` from https://www.jenkins.io/download/
2. Run with: `java -jar jenkins.war --httpPort=8080`

## 🚀 Step 2: Initial Jenkins Setup

1. **Access Jenkins**
   - Open browser: http://localhost:8080
   - You'll see "Unlock Jenkins" screen

2. **Get Initial Admin Password**
   ```powershell
   Get-Content "C:\Program Files\Jenkins\secrets\initialAdminPassword"
   ```
   Or if using WAR file:
   ```powershell
   Get-Content "$env:USERPROFILE\.jenkins\secrets\initialAdminPassword"
   ```

3. **Install Suggested Plugins**
   - Click "Install suggested plugins"
   - Wait for installation to complete

4. **Create Admin User**
   - Username: `admin` (or your preference)
   - Password: (choose a secure password)
   - Full name: Your Name
   - Email: your.email@example.com

## 🔧 Step 3: Install Required Jenkins Plugins

Go to: **Manage Jenkins** → **Plugin Manager** → **Available Plugins**

Search and install these plugins:
- ✅ **Pipeline** (usually pre-installed)
- ✅ **Git** (usually pre-installed)
- ✅ **GitHub Integration**
- ✅ **Maven Integration**
- ✅ **JUnit Plugin**
- ✅ **TestNG Plugin**
- ✅ **Allure Jenkins Plugin**
- ✅ **HTML Publisher**
- ✅ **Blue Ocean** (optional, for better UI)

Click "Install without restart" then restart Jenkins after all plugins are installed.

## ⚙️ Step 4: Configure Jenkins Tools

### Configure JDK
1. Go to: **Manage Jenkins** → **Global Tool Configuration**
2. Scroll to **JDK**
3. Click "Add JDK"
4. Name: `JDK-11` (or `JDK-17`)
5. Either:
   - Uncheck "Install automatically" and set JAVA_HOME: `C:\Program Files\Java\jdk-21`
   - Or keep checked to auto-install

### Configure Maven
1. In same Global Tool Configuration
2. Scroll to **Maven**
3. Click "Add Maven"
4. Name: `Maven-3.9.5`
5. Either:
   - Uncheck "Install automatically" and set MAVEN_HOME: `C:\Users\Brandon\tools\apache-maven-3.9.5`
   - Or keep checked and select version 3.9.5 to auto-install

### Configure Git
1. Scroll to **Git**
2. Path to Git executable: `C:\Program Files\Git\bin\git.exe` (or where git is installed)
3. Click "Save"

## 📦 Step 5: Create Pipeline Job

1. **Create New Job**
   - Click "New Item" from Jenkins home
   - Enter name: `CudaGame-CI-Pipeline`
   - Select "Pipeline"
   - Click OK

2. **Configure Pipeline**

### General Section:
- ✅ GitHub project
- Project url: `https://github.com/bthecobb/CudaGame-CI`

### Build Triggers:
- ✅ GitHub hook trigger for GITScm polling (for automatic builds)
- ✅ Poll SCM: `H/5 * * * *` (check every 5 minutes)

### Pipeline Section:
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/bthecobb/CudaGame-CI.git`
- Credentials: (add if repository is private)
- Branch: `*/master`
- Script Path: `Jenkinsfile`

3. **Click Save**

## 🔐 Step 6: Setup GitHub Credentials (if needed)

1. Go to: **Manage Jenkins** → **Manage Credentials**
2. Click on **(global)** domain
3. Click **Add Credentials**
4. Kind: **Username with password**
   - Username: Your GitHub username
   - Password: GitHub Personal Access Token
   - ID: `github-credentials`
   - Description: GitHub Access

To create GitHub Personal Access Token:
1. Go to GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token with `repo` scope

## 🏃 Step 7: Run Your First Build

1. Go to your pipeline job: `CudaGame-CI-Pipeline`
2. Click **Build Now**
3. Click on the build number (#1)
4. Click **Console Output** to see live logs

## 🔄 Step 8: Setup GitHub Webhook (for automatic builds)

1. Go to your GitHub repository: https://github.com/bthecobb/CudaGame-CI
2. Settings → Webhooks → Add webhook
3. Payload URL: `http://YOUR_JENKINS_URL:8080/github-webhook/`
   - For local: You'll need ngrok or similar to expose local Jenkins
   - For public server: Use your server's URL
4. Content type: `application/json`
5. Which events: Just the push event
6. Active: ✅
7. Add webhook

## 📊 Step 9: View Reports

After build completes:
- **JUnit Results**: Automatically displayed on job page
- **TestNG Results**: Click "TestNG Results" on left menu
- **Allure Reports**: Click "Allure Report" on left menu
- **Console Output**: Full build logs

## 🎨 Step 10: Install Blue Ocean (Optional - Better UI)

1. Install Blue Ocean plugin
2. Click "Open Blue Ocean" from Jenkins home
3. View your pipeline in modern UI

## 🛠️ Troubleshooting

### Common Issues:

**Maven not found:**
- Ensure Maven is in PATH or configured in Global Tool Configuration

**Tests failing:**
- Check Console Output for detailed error messages
- Ensure all dependencies are downloaded

**Permission denied:**
- Run Jenkins service as Administrator
- Check file permissions in workspace

**Port 8080 already in use:**
- Change port: `java -jar jenkins.war --httpPort=9090`
- Or stop conflicting service

## 📝 Useful Jenkins Commands (PowerShell)

```powershell
# Start Jenkins service
Start-Service Jenkins

# Stop Jenkins service
Stop-Service Jenkins

# Restart Jenkins service
Restart-Service Jenkins

# Check Jenkins service status
Get-Service Jenkins

# View Jenkins logs
Get-Content "C:\Program Files\Jenkins\jenkins.err.log" -Tail 50
Get-Content "C:\Program Files\Jenkins\jenkins.out.log" -Tail 50
```

## 🔗 Additional Resources

- Jenkins Documentation: https://www.jenkins.io/doc/
- Pipeline Syntax: https://www.jenkins.io/doc/book/pipeline/syntax/
- Your Repository: https://github.com/bthecobb/CudaGame-CI

---

## ✅ Quick Checklist

- [ ] Jenkins installed and running
- [ ] Admin user created
- [ ] Required plugins installed
- [ ] JDK configured
- [ ] Maven configured
- [ ] Git configured
- [ ] Pipeline job created
- [ ] First build successful
- [ ] Reports visible

Once all items are checked, your Jenkins CI/CD pipeline is fully operational! 🎉
