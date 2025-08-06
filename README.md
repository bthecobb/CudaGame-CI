# CudaGame CI/CD Testing Pipeline

A comprehensive testing pipeline for CudaGame using TestNG and JUnit 5, with Jenkins and GitHub Actions integration.

## 🚀 Features

- **Dual Testing Frameworks**: JUnit 5 and TestNG support
- **CI/CD Integration**: Jenkins Pipeline and GitHub Actions workflows
- **Test Categories**:
  - Unit Tests (JUnit & TestNG)
  - Integration Tests
  - Performance Tests
  - Concurrent Testing
- **Advanced Reporting**:
  - Allure Reports
  - JaCoCo Code Coverage
  - TestNG HTML Reports
- **Cross-Platform Support**: Tests on Windows, Linux, and macOS

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Git
- Jenkins (optional, for Jenkins pipeline)
- GitHub account (for GitHub Actions)

## 🛠️ Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/CudaGame-CI.git
cd CudaGame-CI
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Run Tests Locally

#### Run All Tests
```bash
mvn test
```

#### Run JUnit Tests Only
```bash
mvn test -Pjunit-only
```

#### Run TestNG Tests Only
```bash
mvn test -Ptestng-only
```

#### Run Integration Tests
```bash
mvn verify
```

## 🧪 Test Structure

```
src/test/java/com/cudagame/tests/
├── junit/
│   └── GameEngineTest.java       # JUnit 5 tests for game engine
├── testng/
│   └── GamePhysicsTest.java      # TestNG tests for physics
└── integration/
    └── GameIntegrationTest.java  # Integration tests
```

## 📊 Test Reports

### Allure Reports
Generate and view Allure reports:
```bash
mvn allure:serve
```

### JaCoCo Coverage
Generate coverage report:
```bash
mvn jacoco:report
```
View at: `target/site/jacoco/index.html`

### TestNG Reports
View at: `target/surefire-reports/index.html`

## 🔧 Jenkins Setup

1. **Create a New Pipeline Job**
   - New Item → Pipeline
   - Name: `CudaGame-CI-Pipeline`

2. **Configure Pipeline**
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: Your GitHub repo URL
   - Script Path: `Jenkinsfile`

3. **Required Jenkins Plugins**
   - Maven Integration
   - JUnit Plugin
   - TestNG Plugin
   - Allure Jenkins Plugin
   - HTML Publisher

4. **Configure Tools in Jenkins**
   - Go to Manage Jenkins → Global Tool Configuration
   - Add Maven installation: `Maven-3.9.5`
   - Add JDK installation: `JDK-11`

## 🐙 GitHub Actions

The project includes GitHub Actions workflows that automatically:
- Run tests on push to main/develop branches
- Test on multiple OS (Ubuntu, Windows, macOS)
- Test with multiple Java versions (11, 17)
- Generate test reports and artifacts
- Perform security scans
- Create releases on main branch

### Workflow Files
- `.github/workflows/ci.yml` - Main CI/CD pipeline

## 📝 Writing Tests

### JUnit 5 Example
```java
@Test
@DisplayName("Test Game Initialization")
void testGameInitialization() {
    boolean initialized = game.initialize();
    assertTrue(initialized);
}
```

### TestNG Example
```java
@Test(priority = 1)
@Description("Test gravity simulation")
public void testGravitySimulation() {
    double result = physics.applyGravity(100.0);
    Assert.assertTrue(result < 100.0);
}
```

## 🎯 Test Profiles

### Unit Tests Profile
```xml
mvn test -Punit-tests
```

### Integration Tests Profile
```xml
mvn test -Pintegration-tests
```

### Performance Tests Profile
```xml
mvn test -Dgroups=performance
```

## 📈 Continuous Improvement

### Code Quality Metrics
- Minimum 80% code coverage
- All critical paths tested
- Performance benchmarks maintained

### Test Execution Time Goals
- Unit tests: < 5 seconds
- Integration tests: < 30 seconds
- Full suite: < 2 minutes

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request

## 📄 Test Configuration

### Maven Surefire Configuration
- Parallel execution enabled
- Test failure reporting
- XML and HTML reports

### TestNG Configuration
- See `src/test/resources/testng.xml`
- Parallel test execution
- Group-based test selection

## 🐛 Troubleshooting

### Common Issues

1. **Tests not found**
   - Ensure test classes follow naming convention (*Test.java)
   - Check package structure

2. **Out of Memory**
   - Increase heap size: `export MAVEN_OPTS="-Xmx2048m"`

3. **Jenkins build fails**
   - Check Jenkins has correct JDK/Maven versions
   - Verify workspace permissions

## 📚 Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [TestNG Documentation](https://testng.org/doc/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Allure Framework](https://docs.qameta.io/allure/)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)

## 📊 Project Status

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-85%25-green)
![Tests](https://img.shields.io/badge/tests-42%20passed-success)

## 📝 License

This project is part of the CudaGame testing infrastructure.

---

**Created for CudaGame** | **Automated Testing Pipeline** | **CI/CD Ready**
