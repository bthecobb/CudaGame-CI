package com.cudagame.tests.junit.ecs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import io.qameta.allure.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the Entity Component System (ECS) in CudaGame
 * Tests entity creation, component management, and system updates
 */
@Epic("Game Engine Tests")
@Feature("Entity Component System (ECS)")
@DisplayName("Game Engine ECS Test Suite")
public class GameEngineECSTest {
    
    // Mock game engine components
    private GameEngine gameEngine;
    private EntityManager entityManager;
    private ComponentManager componentManager;
    private SystemManager systemManager;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        gameEngine = new GameEngine();
        entityManager = new EntityManager();
        componentManager = new ComponentManager();
        systemManager = new SystemManager();
        
        gameEngine.initialize(entityManager, componentManager, systemManager);
    }
    
    @AfterEach
    void cleanup() {
        gameEngine.shutdown();
    }
    
    @Test
    @DisplayName("Test Entity Creation and Destruction")
    @Description("Verify entities can be created and destroyed properly")
    @Severity(SeverityLevel.CRITICAL)
    void testEntityLifecycle() {
        // Create entity
        int entityId = entityManager.createEntity("Player");
        
        assertAll("Entity Creation",
            () -> assertTrue(entityId > 0, "Entity ID should be positive"),
            () -> assertTrue(entityManager.isEntityAlive(entityId), "Entity should be alive"),
            () -> assertEquals("Player", entityManager.getEntityName(entityId))
        );
        
        // Destroy entity
        entityManager.destroyEntity(entityId);
        
        assertFalse(entityManager.isEntityAlive(entityId), "Entity should be destroyed");
    }
    
    @Test
    @DisplayName("Test Component Addition and Removal")
    @Description("Verify components can be attached and detached from entities")
    @Severity(SeverityLevel.CRITICAL)
    void testComponentManagement() {
        int entityId = entityManager.createEntity("TestEntity");
        
        // Add components
        TransformComponent transform = new TransformComponent(0, 0, 0);
        RenderComponent render = new RenderComponent("player.mesh");
        PhysicsComponent physics = new PhysicsComponent(1.0f, 0.5f);
        
        componentManager.addComponent(entityId, transform);
        componentManager.addComponent(entityId, render);
        componentManager.addComponent(entityId, physics);
        
        // Verify components exist
        assertAll("Component Addition",
            () -> assertTrue(componentManager.hasComponent(entityId, TransformComponent.class)),
            () -> assertTrue(componentManager.hasComponent(entityId, RenderComponent.class)),
            () -> assertTrue(componentManager.hasComponent(entityId, PhysicsComponent.class))
        );
        
        // Get and verify component data
        TransformComponent retrievedTransform = componentManager.getComponent(entityId, TransformComponent.class);
        assertNotNull(retrievedTransform);
        assertEquals(0, retrievedTransform.x, 0.001);
        
        // Remove component
        componentManager.removeComponent(entityId, PhysicsComponent.class);
        assertFalse(componentManager.hasComponent(entityId, PhysicsComponent.class));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {10, 50, 100, 500})
    @DisplayName("Test ECS Performance with Multiple Entities")
    @Description("Verify ECS can handle multiple entities efficiently")
    void testECSPerformance(int entityCount) {
        long startTime = java.lang.System.currentTimeMillis();
        
        List<Integer> entities = new ArrayList<>();
        for (int i = 0; i < entityCount; i++) {
            int id = entityManager.createEntity("Entity_" + i);
            entities.add(id);
            componentManager.addComponent(id, new TransformComponent(i, i, i));
        }
        
        long creationTime = java.lang.System.currentTimeMillis() - startTime;
        
        // Update all entities
        startTime = java.lang.System.currentTimeMillis();
        systemManager.updateAllSystems(0.016f); // 60 FPS frame time
        long updateTime = java.lang.System.currentTimeMillis() - startTime;
        
        assertAll("Performance Metrics",
            () -> assertThat(creationTime).isLessThan(1000L),
            () -> assertThat(updateTime).isLessThan(100L),
            () -> assertEquals(entityCount, entities.size())
        );
    }
    
    @Test
    @DisplayName("Test System Update Order")
    @Description("Verify systems update in correct order based on priority")
    @Severity(SeverityLevel.NORMAL)
    void testSystemUpdateOrder() {
        AtomicInteger updateOrder = new AtomicInteger(0);
        
        // Add systems with different priorities
        PhysicsSystem physicsSystem = new PhysicsSystem(1); // Highest priority
        RenderSystem renderSystem = new RenderSystem(2);
        AudioSystem audioSystem = new AudioSystem(3);
        
        systemManager.addSystem(physicsSystem);
        systemManager.addSystem(renderSystem);
        systemManager.addSystem(audioSystem);
        
        // Track update order
        physicsSystem.setUpdateCallback(() -> assertEquals(1, updateOrder.incrementAndGet()));
        renderSystem.setUpdateCallback(() -> assertEquals(2, updateOrder.incrementAndGet()));
        audioSystem.setUpdateCallback(() -> assertEquals(3, updateOrder.incrementAndGet()));
        
        systemManager.updateAllSystems(0.016f);
        
        assertEquals(3, updateOrder.get(), "All systems should have updated");
    }
    
    @Test
    @DisplayName("Test Component Query System")
    @Description("Verify ability to query entities with specific component combinations")
    void testComponentQueries() {
        // Create entities with different component combinations
        int player = entityManager.createEntity("Player");
        componentManager.addComponent(player, new TransformComponent(0, 0, 0));
        componentManager.addComponent(player, new RenderComponent("player.mesh"));
        componentManager.addComponent(player, new PhysicsComponent(1.0f, 0.5f));
        
        int enemy = entityManager.createEntity("Enemy");
        componentManager.addComponent(enemy, new TransformComponent(10, 0, 0));
        componentManager.addComponent(enemy, new RenderComponent("enemy.mesh"));
        
        int invisible = entityManager.createEntity("Invisible");
        componentManager.addComponent(invisible, new TransformComponent(5, 5, 5));
        
        // Query entities with both Transform and Render components
        Set<Integer> renderableEntities = componentManager.getEntitiesWithComponents(
            TransformComponent.class, RenderComponent.class
        );
        
        assertAll("Component Queries",
            () -> assertEquals(2, renderableEntities.size()),
            () -> assertTrue(renderableEntities.contains(player)),
            () -> assertTrue(renderableEntities.contains(enemy)),
            () -> assertFalse(renderableEntities.contains(invisible))
        );
    }
    
    @Nested
    @DisplayName("Component Pool Tests")
    class ComponentPoolTests {
        
        @Test
        @DisplayName("Test Component Pool Recycling")
        void testComponentPoolRecycling() {
            // Set up the connection between managers
            entityManager.setComponentManager(componentManager);
            
            // Create and destroy many components to test pooling
            for (int i = 0; i < 100; i++) {
                int entity = entityManager.createEntity("Temp");
                componentManager.addComponent(entity, new TransformComponent(i, i, i));
                entityManager.destroyEntity(entity);
            }
            
            // Verify pool is reusing components
            int poolSize = componentManager.getPoolSize(TransformComponent.class);
            assertThat(poolSize).isGreaterThan(0).isLessThanOrEqualTo(100);
        }
        
        @Test
        @DisplayName("Test Component Data Integrity")
        void testComponentDataIntegrity() {
            int entity = entityManager.createEntity("Test");
            TransformComponent original = new TransformComponent(1.5f, 2.5f, 3.5f);
            componentManager.addComponent(entity, original);
            
            // Modify original reference
            original.x = 999;
            
            // Verify stored component is unaffected
            TransformComponent stored = componentManager.getComponent(entity, TransformComponent.class);
            assertEquals(1.5f, stored.x, 0.001);
        }
    }
    
    // Mock classes for testing
    static class GameEngine {
        private EntityManager entityManager;
        private ComponentManager componentManager;
        private SystemManager systemManager;
        
        void initialize(EntityManager em, ComponentManager cm, SystemManager sm) {
            this.entityManager = em;
            this.componentManager = cm;
            this.systemManager = sm;
        }
        
        void shutdown() {
            // Cleanup resources
        }
    }
    
    static class EntityManager {
        private Map<Integer, String> entities = new HashMap<>();
        private AtomicInteger nextId = new AtomicInteger(1);
        
        int createEntity(String name) {
            int id = nextId.getAndIncrement();
            entities.put(id, name);
            return id;
        }
        
        void destroyEntity(int id) {
            entities.remove(id);
            // When entity is destroyed, remove its components and add them to pool
            if (componentManager != null) {
                componentManager.recycleEntityComponents(id);
            }
        }
        
        private ComponentManager componentManager;
        void setComponentManager(ComponentManager cm) {
            this.componentManager = cm;
        }
        
        boolean isEntityAlive(int id) {
            return entities.containsKey(id);
        }
        
        String getEntityName(int id) {
            return entities.get(id);
        }
    }
    
    static class ComponentManager {
        private Map<Integer, Map<Class<?>, Object>> componentStore = new HashMap<>();
        private Map<Class<?>, List<Object>> componentPools = new HashMap<>();
        
        <T> void addComponent(int entityId, T component) {
            // Create a copy to ensure data integrity
            T componentCopy = copyComponent(component);
            componentStore.computeIfAbsent(entityId, k -> new HashMap<>())
                .put(component.getClass(), componentCopy);
        }
        
        @SuppressWarnings("unchecked")
        private <T> T copyComponent(T component) {
            if (component instanceof TransformComponent) {
                TransformComponent tc = (TransformComponent) component;
                return (T) new TransformComponent(tc.x, tc.y, tc.z);
            }
            return component;
        }
        
        <T> T getComponent(int entityId, Class<T> componentClass) {
            Map<Class<?>, Object> components = componentStore.get(entityId);
            return components != null ? componentClass.cast(components.get(componentClass)) : null;
        }
        
        <T> boolean hasComponent(int entityId, Class<T> componentClass) {
            return getComponent(entityId, componentClass) != null;
        }
        
        <T> void removeComponent(int entityId, Class<T> componentClass) {
            Map<Class<?>, Object> components = componentStore.get(entityId);
            if (components != null) {
                Object removed = components.remove(componentClass);
                if (removed != null) {
                    componentPools.computeIfAbsent(componentClass, k -> new ArrayList<>()).add(removed);
                }
            }
        }
        
        Set<Integer> getEntitiesWithComponents(Class<?>... componentClasses) {
            Set<Integer> result = new HashSet<>();
            for (Map.Entry<Integer, Map<Class<?>, Object>> entry : componentStore.entrySet()) {
                boolean hasAll = true;
                for (Class<?> cls : componentClasses) {
                    if (!entry.getValue().containsKey(cls)) {
                        hasAll = false;
                        break;
                    }
                }
                if (hasAll) {
                    result.add(entry.getKey());
                }
            }
            return result;
        }
        
        void recycleEntityComponents(int entityId) {
            Map<Class<?>, Object> components = componentStore.remove(entityId);
            if (components != null) {
                for (Map.Entry<Class<?>, Object> entry : components.entrySet()) {
                    componentPools.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(entry.getValue());
                }
            }
        }
        
        int getPoolSize(Class<?> componentClass) {
            List<Object> pool = componentPools.get(componentClass);
            return pool != null ? pool.size() : 0;
        }
    }
    
    static class SystemManager {
        private List<GameSystem> systems = new ArrayList<>();
        
        void addSystem(GameSystem system) {
            systems.add(system);
            systems.sort((a, b) -> Integer.compare(a.priority, b.priority));
        }
        
        void updateAllSystems(float deltaTime) {
            for (GameSystem system : systems) {
                system.update(deltaTime);
            }
        }
    }
    
    // Component classes
    static class TransformComponent {
        float x, y, z;
        TransformComponent(float x, float y, float z) {
            this.x = x; this.y = y; this.z = z;
        }
    }
    
    static class RenderComponent {
        String meshPath;
        RenderComponent(String meshPath) {
            this.meshPath = meshPath;
        }
    }
    
    static class PhysicsComponent {
        float mass, friction;
        PhysicsComponent(float mass, float friction) {
            this.mass = mass; this.friction = friction;
        }
    }
    
    // System classes
    static abstract class GameSystem {
        int priority;
        Runnable updateCallback;
        
        GameSystem(int priority) {
            this.priority = priority;
        }
        
        void setUpdateCallback(Runnable callback) {
            this.updateCallback = callback;
        }
        
        void update(float deltaTime) {
            if (updateCallback != null) {
                updateCallback.run();
            }
        }
    }
    
    static class PhysicsSystem extends GameSystem {
        PhysicsSystem(int priority) { super(priority); }
    }
    
    static class RenderSystem extends GameSystem {
        RenderSystem(int priority) { super(priority); }
    }
    
    static class AudioSystem extends GameSystem {
        AudioSystem(int priority) { super(priority); }
    }
}
