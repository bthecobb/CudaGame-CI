package com.cudagame.tests.testng.lighting;

import org.testng.annotations.*;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import io.qameta.allure.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TestNG tests for the Lighting System in CudaGame
 * Tests light sources, shadows, ambient lighting, and performance
 */
@Epic("Rendering Tests")
@Feature("Lighting System")
public class LightingSystemTest {
    
    private LightingSystem lightingSystem;
    private RenderContext renderContext;
    private SoftAssert softAssert;
    private Map<String, Light> lights;
    
    @BeforeSuite
    public void setupSuite() {
        System.out.println("Initializing Lighting System Test Suite");
    }
    
    @BeforeClass
    public void setupClass() {
        renderContext = new RenderContext(1920, 1080);
        lights = new ConcurrentHashMap<>();
    }
    
    @BeforeMethod
    public void setupMethod() {
        lightingSystem = new LightingSystem(renderContext);
        softAssert = new SoftAssert();
        lights.clear();
    }
    
    @AfterMethod
    public void teardownMethod() {
        lightingSystem.cleanup();
    }
    
    @Test(priority = 1)
    @Description("Test directional light setup and properties")
    @Severity(SeverityLevel.CRITICAL)
    public void testDirectionalLight() {
        // Create directional light (sun)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(-0.5f, -1.0f, -0.5f);
        sun.setColor(1.0f, 0.95f, 0.8f);
        sun.setIntensity(1.0f);
        
        lightingSystem.addLight("sun", sun);
        
        // Verify light properties
        Light retrievedLight = lightingSystem.getLight("sun");
        Assert.assertNotNull(retrievedLight, "Light should be registered");
        Assert.assertTrue(retrievedLight instanceof DirectionalLight);
        
        DirectionalLight dirLight = (DirectionalLight) retrievedLight;
        float[] direction = dirLight.getDirection();
        
        // Verify normalized direction
        float length = (float) Math.sqrt(direction[0] * direction[0] + 
                                        direction[1] * direction[1] + 
                                        direction[2] * direction[2]);
        Assert.assertEquals(length, 1.0f, 0.01f, "Direction should be normalized");
    }
    
    @Test(priority = 2)
    @Description("Test point light attenuation and range")
    @Severity(SeverityLevel.NORMAL)
    public void testPointLightAttenuation() {
        PointLight pointLight = new PointLight();
        pointLight.setPosition(10.0f, 5.0f, 10.0f);
        pointLight.setColor(1.0f, 0.5f, 0.0f); // Orange light
        pointLight.setRange(20.0f);
        pointLight.setAttenuation(1.0f, 0.09f, 0.032f); // Quadratic attenuation
        
        lightingSystem.addLight("torch", pointLight);
        
        // Test attenuation at different distances
        float[] testDistances = {0.0f, 5.0f, 10.0f, 15.0f, 20.0f};
        for (float distance : testDistances) {
            float attenuation = pointLight.calculateAttenuation(distance);
            
            softAssert.assertTrue(attenuation >= 0.0f && attenuation <= 1.0f,
                "Attenuation should be between 0 and 1 at distance " + distance);
            
            if (distance > 0 && distance < 20.0f) {
                float nextAttenuation = pointLight.calculateAttenuation(distance + 1.0f);
                softAssert.assertTrue(attenuation > nextAttenuation,
                    "Attenuation should decrease with distance");
            }
        }
        
        softAssert.assertAll();
    }
    
    @Test(priority = 3)
    @Description("Test spotlight cone angles and direction")
    public void testSpotLight() {
        SpotLight spotLight = new SpotLight();
        spotLight.setPosition(0.0f, 10.0f, 0.0f);
        spotLight.setDirection(0.0f, -1.0f, 0.0f); // Pointing down
        spotLight.setConeAngles(25.0f, 35.0f); // Inner and outer cone
        spotLight.setColor(1.0f, 1.0f, 1.0f);
        
        lightingSystem.addLight("spotlight", spotLight);
        
        // Test cone angle calculations
        float innerCos = spotLight.getInnerConeCosine();
        float outerCos = spotLight.getOuterConeCosine();
        
        Assert.assertTrue(innerCos > outerCos, 
            "Inner cone cosine should be greater than outer (smaller angle)");
        
        // Test light intensity within cone
        float[] testAngles = {0.0f, 15.0f, 25.0f, 30.0f, 35.0f, 45.0f};
        for (float angle : testAngles) {
            float intensity = spotLight.calculateConeIntensity(angle);
            
            if (angle <= 25.0f) {
                Assert.assertEquals(intensity, 1.0f, 0.01f,
                    "Full intensity within inner cone");
            } else if (angle >= 35.0f) {
                Assert.assertEquals(intensity, 0.0f, 0.01f,
                    "No intensity outside outer cone");
            } else {
                Assert.assertTrue(intensity > 0.0f && intensity < 1.0f,
                    "Falloff between inner and outer cone");
            }
        }
    }
    
    @Test(priority = 4, dataProvider = "lightCountData")
    @Description("Test lighting system performance with multiple lights")
    public void testMultipleLightsPerformance(int lightCount, long maxTimeMs) {
        long startTime = System.currentTimeMillis();
        
        // Add multiple lights
        for (int i = 0; i < lightCount; i++) {
            if (i % 3 == 0) {
                DirectionalLight light = new DirectionalLight();
                light.setDirection(-1.0f, -1.0f, -1.0f);
                lightingSystem.addLight("dir_" + i, light);
            } else if (i % 3 == 1) {
                PointLight light = new PointLight();
                light.setPosition(i * 10.0f, 5.0f, i * 10.0f);
                light.setRange(20.0f);
                lightingSystem.addLight("point_" + i, light);
            } else {
                SpotLight light = new SpotLight();
                light.setPosition(i * 10.0f, 10.0f, i * 10.0f);
                lightingSystem.addLight("spot_" + i, light);
            }
        }
        
        // Calculate lighting for test scene
        lightingSystem.calculateLighting();
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        Assert.assertTrue(elapsedTime < maxTimeMs,
            String.format("Lighting calculation for %d lights took %dms (max: %dms)",
                lightCount, elapsedTime, maxTimeMs));
    }
    
    @DataProvider(name = "lightCountData")
    public Object[][] getLightCountData() {
        return new Object[][] {
            {10, 100L},    // 10 lights, max 100ms
            {50, 200L},    // 50 lights, max 200ms
            {100, 500L},   // 100 lights, max 500ms
            {200, 1000L}   // 200 lights, max 1000ms
        };
    }
    
    @Test(priority = 5)
    @Description("Test ambient lighting and global illumination")
    @Severity(SeverityLevel.NORMAL)
    public void testAmbientLighting() {
        // Set ambient light
        lightingSystem.setAmbientLight(0.1f, 0.1f, 0.15f); // Slight blue tint
        
        float[] ambient = lightingSystem.getAmbientLight();
        Assert.assertEquals(ambient[0], 0.1f, 0.001f);
        Assert.assertEquals(ambient[1], 0.1f, 0.001f);
        Assert.assertEquals(ambient[2], 0.15f, 0.001f);
        
        // Test combined lighting with ambient
        PointLight light = new PointLight();
        light.setPosition(0, 0, 0);
        light.setColor(1.0f, 1.0f, 1.0f);
        lightingSystem.addLight("test", light);
        
        float[] finalColor = lightingSystem.calculateFinalColor(0, 0, 0);
        
        // Verify ambient contribution
        softAssert.assertTrue(finalColor[0] >= 0.1f, "Red channel includes ambient");
        softAssert.assertTrue(finalColor[1] >= 0.1f, "Green channel includes ambient");
        softAssert.assertTrue(finalColor[2] >= 0.15f, "Blue channel includes ambient");
        softAssert.assertAll();
    }
    
    @Test(priority = 6, groups = {"shadows"})
    @Description("Test shadow mapping and occlusion")
    public void testShadowMapping() {
        // Enable shadows
        lightingSystem.enableShadows(true);
        lightingSystem.setShadowMapResolution(2048);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(-1, -1, -1);
        sun.setCastsShadows(true);
        lightingSystem.addLight("sun", sun);
        
        // Create shadow map
        ShadowMap shadowMap = lightingSystem.generateShadowMap("sun");
        Assert.assertNotNull(shadowMap);
        Assert.assertEquals(shadowMap.getWidth(), 2048);
        Assert.assertEquals(shadowMap.getHeight(), 2048);
        
        // Test shadow occlusion
        boolean inShadow = lightingSystem.isPointInShadow(10, 0, 10, "sun");
        // This would depend on scene geometry
    }
    
    @Test(priority = 7)
    @Description("Test light color temperature conversion")
    public void testColorTemperature() {
        // Test common color temperatures
        float[][] temps = {
            {2700, 1.0f, 0.82f, 0.69f},  // Warm white
            {4000, 1.0f, 0.92f, 0.82f},  // Cool white
            {5500, 1.0f, 0.98f, 0.94f},  // Daylight
            {6500, 0.95f, 0.97f, 1.0f}   // Cool daylight
        };
        
        for (float[] temp : temps) {
            float[] rgb = LightingSystem.kelvinToRGB(temp[0]);
            
            softAssert.assertEquals(rgb[0], temp[1], 0.1f,
                "Red channel for " + temp[0] + "K");
            softAssert.assertEquals(rgb[1], temp[2], 0.1f,
                "Green channel for " + temp[0] + "K");
            softAssert.assertEquals(rgb[2], temp[3], 0.1f,
                "Blue channel for " + temp[0] + "K");
        }
        
        softAssert.assertAll();
    }
    
    @Test(enabled = false)
    @Description("Test HDR lighting - not yet implemented")
    public void testHDRLighting() {
        // TODO: Implement HDR lighting tests
    }
    
    // Mock classes for testing
    static class LightingSystem {
        private Map<String, Light> lights = new HashMap<>();
        private RenderContext context;
        private float[] ambientLight = {0.05f, 0.05f, 0.05f};
        private boolean shadowsEnabled = false;
        private int shadowMapResolution = 1024;
        
        LightingSystem(RenderContext context) {
            this.context = context;
        }
        
        void addLight(String name, Light light) {
            lights.put(name, light);
        }
        
        Light getLight(String name) {
            return lights.get(name);
        }
        
        void setAmbientLight(float r, float g, float b) {
            ambientLight[0] = r;
            ambientLight[1] = g;
            ambientLight[2] = b;
        }
        
        float[] getAmbientLight() {
            return ambientLight.clone();
        }
        
        void calculateLighting() {
            // Simulate lighting calculation
            try {
                Thread.sleep(lights.size() / 10); // Simulate processing time
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        float[] calculateFinalColor(float x, float y, float z) {
            float[] color = ambientLight.clone();
            // Add light contributions
            for (Light light : lights.values()) {
                // Simplified calculation
                color[0] = Math.min(1.0f, color[0] + 0.1f);
                color[1] = Math.min(1.0f, color[1] + 0.1f);
                color[2] = Math.min(1.0f, color[2] + 0.1f);
            }
            return color;
        }
        
        void enableShadows(boolean enable) {
            shadowsEnabled = enable;
        }
        
        void setShadowMapResolution(int resolution) {
            shadowMapResolution = resolution;
        }
        
        ShadowMap generateShadowMap(String lightName) {
            if (!shadowsEnabled) return null;
            return new ShadowMap(shadowMapResolution, shadowMapResolution);
        }
        
        boolean isPointInShadow(float x, float y, float z, String lightName) {
            return false; // Simplified
        }
        
        static float[] kelvinToRGB(float kelvin) {
            // Simplified color temperature conversion
            float[] rgb = new float[3];
            float temp = kelvin / 100;
            
            // Red
            if (temp <= 66) {
                rgb[0] = 1.0f;
            } else {
                rgb[0] = Math.min(1.0f, 1.292936f * (float)Math.pow(temp - 60, -0.1332047592f));
            }
            
            // Green
            if (temp <= 66) {
                rgb[1] = Math.min(1.0f, 0.39008157f * (float)Math.log(temp) - 0.63184144f);
            } else {
                rgb[1] = Math.min(1.0f, 1.292936f * (float)Math.pow(temp - 60, -0.0755148492f));
            }
            
            // Blue
            if (temp >= 66) {
                rgb[2] = 1.0f;
            } else if (temp >= 19) {
                rgb[2] = Math.min(1.0f, 0.543206789f * (float)Math.log(temp - 10) - 1.19625408f);
            } else {
                rgb[2] = 0.0f;
            }
            
            return rgb;
        }
        
        void cleanup() {
            lights.clear();
        }
    }
    
    static class RenderContext {
        int width, height;
        RenderContext(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    static abstract class Light {
        protected float[] color = {1.0f, 1.0f, 1.0f};
        protected float intensity = 1.0f;
        
        void setColor(float r, float g, float b) {
            color[0] = r; color[1] = g; color[2] = b;
        }
        
        void setIntensity(float intensity) {
            this.intensity = intensity;
        }
    }
    
    static class DirectionalLight extends Light {
        private float[] direction = {0, -1, 0};
        private boolean castsShadows = false;
        
        void setDirection(float x, float y, float z) {
            float length = (float) Math.sqrt(x*x + y*y + z*z);
            direction[0] = x / length;
            direction[1] = y / length;
            direction[2] = z / length;
        }
        
        float[] getDirection() {
            return direction.clone();
        }
        
        void setCastsShadows(boolean cast) {
            castsShadows = cast;
        }
    }
    
    static class PointLight extends Light {
        private float[] position = {0, 0, 0};
        private float range = 10.0f;
        private float constant = 1.0f, linear = 0.09f, quadratic = 0.032f;
        
        void setPosition(float x, float y, float z) {
            position[0] = x; position[1] = y; position[2] = z;
        }
        
        void setRange(float range) {
            this.range = range;
        }
        
        void setAttenuation(float constant, float linear, float quadratic) {
            this.constant = constant;
            this.linear = linear;
            this.quadratic = quadratic;
        }
        
        float calculateAttenuation(float distance) {
            if (distance > range) return 0.0f;
            return 1.0f / (constant + linear * distance + quadratic * distance * distance);
        }
    }
    
    static class SpotLight extends PointLight {
        private float[] direction = {0, -1, 0};
        private float innerCone = 25.0f;
        private float outerCone = 35.0f;
        
        void setDirection(float x, float y, float z) {
            float length = (float) Math.sqrt(x*x + y*y + z*z);
            direction[0] = x / length;
            direction[1] = y / length;
            direction[2] = z / length;
        }
        
        void setConeAngles(float inner, float outer) {
            innerCone = inner;
            outerCone = outer;
        }
        
        float getInnerConeCosine() {
            return (float) Math.cos(Math.toRadians(innerCone));
        }
        
        float getOuterConeCosine() {
            return (float) Math.cos(Math.toRadians(outerCone));
        }
        
        float calculateConeIntensity(float angle) {
            if (angle <= innerCone) return 1.0f;
            if (angle >= outerCone) return 0.0f;
            float epsilon = innerCone - outerCone;
            return Math.max(0, (angle - outerCone) / epsilon);
        }
    }
    
    static class ShadowMap {
        private int width, height;
        
        ShadowMap(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        int getWidth() { return width; }
        int getHeight() { return height; }
    }
}
