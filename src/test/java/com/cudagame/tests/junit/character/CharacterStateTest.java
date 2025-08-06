package com.cudagame.tests.junit.character;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import io.qameta.allure.*;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Character State Management and Player Systems
 * Tests state transitions, animations, combat states, and movement
 */
@Epic("Character System Tests")
@Feature("Character State Management")
@DisplayName("Character State Test Suite")
public class CharacterStateTest {
    
    private Player player;
    private CharacterStateMachine stateMachine;
    private AnimationController animController;
    
    @BeforeEach
    void setup() {
        player = new Player("TestPlayer");
        stateMachine = new CharacterStateMachine(player);
        animController = new AnimationController();
        player.setAnimationController(animController);
    }
    
    @Test
    @DisplayName("Test Initial Character State")
    @Description("Verify character starts in correct initial state")
    @Severity(SeverityLevel.CRITICAL)
    void testInitialState() {
        assertEquals(CharacterState.IDLE, stateMachine.getCurrentState());
        assertTrue(player.isAlive());
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getStamina());
    }
    
    @ParameterizedTest
    @EnumSource(CharacterState.class)
    @DisplayName("Test All State Transitions")
    @Description("Verify all character states can be entered and exited")
    void testStateTransitions(CharacterState targetState) {
        // Skip death state for this test
        if (targetState == CharacterState.DEAD) {
            player.takeDamage(200);
        } else {
            stateMachine.transitionTo(targetState);
        }
        
        CharacterState currentState = stateMachine.getCurrentState();
        
        if (targetState == CharacterState.DEAD) {
            assertEquals(CharacterState.DEAD, currentState);
            assertFalse(player.isAlive());
        } else {
            assertEquals(targetState, currentState);
            
            // Verify animation matches state
            String expectedAnim = getExpectedAnimation(targetState);
            assertEquals(expectedAnim, animController.getCurrentAnimation());
        }
    }
    
    @Test
    @DisplayName("Test Movement State Transitions")
    @Description("Verify movement states transition correctly")
    void testMovementStates() {
        // Idle to Walk
        player.move(1.0f, 0.0f);
        assertEquals(CharacterState.WALKING, stateMachine.getCurrentState());
        
        // Walk to Run
        player.setRunning(true);
        player.move(1.0f, 0.0f);
        assertEquals(CharacterState.RUNNING, stateMachine.getCurrentState());
        
        // Run to Sprint (with stamina)
        player.setSprinting(true);
        player.move(1.0f, 0.0f);
        assertEquals(CharacterState.SPRINTING, stateMachine.getCurrentState());
        
        // Sprint depletes stamina
        for (int i = 0; i < 100; i++) {
            player.update(0.016f); // Simulate frames
        }
        assertThat(player.getStamina()).isLessThan(100);
        
        // Stop moving returns to idle
        player.move(0.0f, 0.0f);
        player.setRunning(false);
        player.setSprinting(false);
        assertEquals(CharacterState.IDLE, stateMachine.getCurrentState());
    }
    
    @Test
    @DisplayName("Test Jump and Aerial States")
    @Description("Verify jump mechanics and aerial state transitions")
    void testJumpMechanics() {
        // Ground jump
        player.jump();
        assertEquals(CharacterState.JUMPING, stateMachine.getCurrentState());
        assertTrue(player.isAirborne());
        
        // Can't jump while airborne
        float initialVelocity = player.getVerticalVelocity();
        player.jump();
        assertEquals(initialVelocity, player.getVerticalVelocity());
        
        // Falling state
        player.setVerticalVelocity(-5.0f);
        stateMachine.update();
        assertEquals(CharacterState.FALLING, stateMachine.getCurrentState());
        
        // Landing
        player.land();
        assertEquals(CharacterState.IDLE, stateMachine.getCurrentState());
        assertFalse(player.isAirborne());
        
        // Double jump (if enabled)
        player.enableDoubleJump(true);
        player.jump();
        player.jump(); // Second jump in air
        assertEquals(1, player.getJumpsRemaining());
    }
    
    @Test
    @DisplayName("Test Combat State Transitions")
    @Description("Verify combat states and attack chains")
    @Severity(SeverityLevel.CRITICAL)
    void testCombatStates() {
        // Basic attack
        player.attack();
        assertEquals(CharacterState.ATTACKING, stateMachine.getCurrentState());
        
        // Combo system
        player.attack();
        player.attack();
        player.attack();
        assertEquals(3, player.getComboCount());
        
        // Blocking
        player.setBlocking(true);
        stateMachine.transitionTo(CharacterState.BLOCKING);
        assertEquals(CharacterState.BLOCKING, stateMachine.getCurrentState());
        
        // Can't attack while blocking
        player.attack();
        assertEquals(CharacterState.BLOCKING, stateMachine.getCurrentState());
        
        // Dodging
        player.setBlocking(false);
        player.dodge();
        assertEquals(CharacterState.DODGING, stateMachine.getCurrentState());
        assertTrue(player.isInvulnerable());
        
        // Dodge has i-frames
        player.takeDamage(50);
        assertEquals(100, player.getHealth()); // No damage during dodge
    }
    
    @ParameterizedTest
    @CsvSource({
        "100, 0, false",    // Full health, not stunned
        "50, 0, false",     // Half health, not stunned
        "25, 75, true",     // Low health, heavy damage, stunned
        "0, 100, true"      // Dead
    })
    @DisplayName("Test Damage and Stun States")
    void testDamageAndStun(int remainingHealth, int damage, boolean shouldStun) {
        player.setHealth(remainingHealth);
        player.takeDamage(damage);
        
        if (remainingHealth - damage <= 0) {
            assertEquals(CharacterState.DEAD, stateMachine.getCurrentState());
            assertFalse(player.isAlive());
        } else if (shouldStun) {
            assertEquals(CharacterState.STUNNED, stateMachine.getCurrentState());
            assertFalse(player.canMove());
            assertFalse(player.canAttack());
        } else {
            assertEquals(CharacterState.HURT, stateMachine.getCurrentState());
        }
    }
    
    @Test
    @DisplayName("Test Special Abilities and Cooldowns")
    @Description("Verify special ability states and cooldown management")
    void testSpecialAbilities() {
        // Use special ability
        player.useAbility("fireball");
        assertEquals(CharacterState.CASTING, stateMachine.getCurrentState());
        
        // Ability on cooldown
        assertTrue(player.isAbilityOnCooldown("fireball"));
        
        // Can't use while on cooldown
        player.useAbility("fireball");
        assertNotEquals(CharacterState.CASTING, stateMachine.getCurrentState());
        
        // Cooldown expires
        player.updateCooldowns(5.0f); // 5 seconds pass
        assertFalse(player.isAbilityOnCooldown("fireball"));
        
        // Ultimate ability
        player.chargeUltimate(100);
        assertTrue(player.canUseUltimate());
        player.useUltimate();
        assertEquals(CharacterState.ULTIMATE, stateMachine.getCurrentState());
    }
    
    @Nested
    @DisplayName("State Priority Tests")
    class StatePriorityTests {
        
        @Test
        @DisplayName("Test State Override Priority")
        void testStateOverridePriority() {
            // Death overrides all states
            player.setStateMachine(stateMachine);
            stateMachine.transitionTo(CharacterState.RUNNING);
            player.takeDamage(200);
            assertEquals(CharacterState.DEAD, stateMachine.getCurrentState());
            
            // Stunned overrides movement
            player.reset();
            stateMachine.transitionTo(CharacterState.RUNNING);
            player.stun(1.0f);
            assertEquals(CharacterState.STUNNED, stateMachine.getCurrentState());
        }
        
        @Test
        @DisplayName("Test Concurrent State Flags")
        void testConcurrentStateFlags() {
            // Can be crouching while moving
            player.setCrouching(true);
            player.move(1.0f, 0.0f);
            assertTrue(player.isCrouching());
            assertEquals(CharacterState.CROUCHING, stateMachine.getCurrentState());
            
            // Can be aiming while moving
            player.setCrouching(false);
            player.setAiming(true);
            player.move(0.5f, 0.0f);
            assertTrue(player.isAiming());
            assertEquals(0.5f, player.getMovementSpeed()); // Reduced speed while aiming
        }
    }
    
    @Test
    @DisplayName("Test State Duration and Timeouts")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testStateDurationAndTimeouts() {
        // Attack state has duration
        player.attack();
        assertEquals(CharacterState.ATTACKING, stateMachine.getCurrentState());
        
        // Simulate attack duration
        for (int i = 0; i < 30; i++) { // 0.5 seconds at 60 FPS
            player.update(0.016f);
        }
        
        // Should return to idle after attack completes
        assertEquals(CharacterState.IDLE, stateMachine.getCurrentState());
        
        // Stun has timeout
        player.stun(1.0f);
        assertEquals(CharacterState.STUNNED, stateMachine.getCurrentState());
        
        // Wait for stun to expire
        player.update(1.1f);
        assertNotEquals(CharacterState.STUNNED, stateMachine.getCurrentState());
    }
    
    // Helper method
    private String getExpectedAnimation(CharacterState state) {
        switch (state) {
            case IDLE: return "idle";
            case WALKING: return "walk";
            case RUNNING: return "run";
            case SPRINTING: return "sprint";
            case JUMPING: return "jump";
            case FALLING: return "fall";
            case ATTACKING: return "attack";
            case BLOCKING: return "block";
            case DODGING: return "dodge";
            case HURT: return "hurt";
            case STUNNED: return "stunned";
            case CASTING: return "cast";
            case ULTIMATE: return "ultimate";
            case CROUCHING: return "crouch";
            case DEAD: return "death";
            default: return "idle";
        }
    }
    
    // Mock classes
    enum CharacterState {
        IDLE, WALKING, RUNNING, SPRINTING, JUMPING, FALLING,
        ATTACKING, BLOCKING, DODGING, HURT, STUNNED,
        CASTING, ULTIMATE, CROUCHING, DEAD
    }
    
    static class Player {
        private String name;
        private int health = 100;
        private int stamina = 100;
        private float ultimateCharge = 0;
        private CharacterState state = CharacterState.IDLE;
        private CharacterStateMachine stateMachine;
        private AnimationController animController;
        private boolean alive = true;
        private boolean airborne = false;
        private boolean running = false;
        private boolean sprinting = false;
        private boolean blocking = false;
        private boolean crouching = false;
        private boolean aiming = false;
        private boolean invulnerable = false;
        private boolean doubleJumpEnabled = false;
        private int jumpsRemaining = 1;
        private int comboCount = 0;
        private float verticalVelocity = 0;
        private float stunDuration = 0;
        
        Player(String name) { this.name = name; }
        
        void setStateMachine(CharacterStateMachine sm) { this.stateMachine = sm; }
        void setAnimationController(AnimationController ac) { this.animController = ac; }
        
        int getHealth() { return health; }
        void setHealth(int h) { health = h; }
        int getStamina() { return stamina; }
        boolean isAlive() { return alive; }
        boolean isAirborne() { return airborne; }
        float getVerticalVelocity() { return verticalVelocity; }
        void setVerticalVelocity(float v) { verticalVelocity = v; }
        int getComboCount() { return comboCount; }
        boolean isInvulnerable() { return invulnerable; }
        boolean isCrouching() { return crouching; }
        boolean isAiming() { return aiming; }
        float getMovementSpeed() { return aiming ? 0.5f : 1.0f; }
        int getJumpsRemaining() { return jumpsRemaining; }
        
        void move(float x, float y) {
            if (x != 0 || y != 0) {
                if (sprinting) state = CharacterState.SPRINTING;
                else if (running) state = CharacterState.RUNNING;
                else if (crouching) state = CharacterState.CROUCHING;
                else state = CharacterState.WALKING;
            } else {
                state = CharacterState.IDLE;
            }
        }
        
        void setRunning(boolean r) { running = r; }
        void setSprinting(boolean s) { sprinting = s; }
        void setBlocking(boolean b) { blocking = b; }
        void setCrouching(boolean c) { crouching = c; }
        void setAiming(boolean a) { aiming = a; }
        void enableDoubleJump(boolean e) { doubleJumpEnabled = e; }
        
        void jump() {
            if (!airborne || (doubleJumpEnabled && jumpsRemaining > 0)) {
                verticalVelocity = 10.0f;
                airborne = true;
                state = CharacterState.JUMPING;
                if (airborne) jumpsRemaining--;
            }
        }
        
        void land() {
            airborne = false;
            jumpsRemaining = doubleJumpEnabled ? 2 : 1;
            state = CharacterState.IDLE;
        }
        
        void attack() {
            if (!blocking && state != CharacterState.STUNNED) {
                state = CharacterState.ATTACKING;
                comboCount++;
            }
        }
        
        void dodge() {
            state = CharacterState.DODGING;
            invulnerable = true;
        }
        
        void takeDamage(int damage) {
            if (!invulnerable) {
                health -= damage;
                if (health <= 0) {
                    health = 0;
                    alive = false;
                    state = CharacterState.DEAD;
                } else if (damage >= 75) {
                    state = CharacterState.STUNNED;
                    stunDuration = 1.0f;
                } else {
                    state = CharacterState.HURT;
                }
            }
        }
        
        void stun(float duration) {
            state = CharacterState.STUNNED;
            stunDuration = duration;
        }
        
        boolean canMove() { return state != CharacterState.STUNNED && alive; }
        boolean canAttack() { return state != CharacterState.STUNNED && alive; }
        
        void useAbility(String ability) {
            if (!isAbilityOnCooldown(ability)) {
                state = CharacterState.CASTING;
            }
        }
        
        boolean isAbilityOnCooldown(String ability) {
            return false; // Simplified
        }
        
        void updateCooldowns(float deltaTime) {
            // Update ability cooldowns
        }
        
        void chargeUltimate(float amount) {
            ultimateCharge = Math.min(100, ultimateCharge + amount);
        }
        
        boolean canUseUltimate() { return ultimateCharge >= 100; }
        
        void useUltimate() {
            if (canUseUltimate()) {
                state = CharacterState.ULTIMATE;
                ultimateCharge = 0;
            }
        }
        
        void update(float deltaTime) {
            if (stunDuration > 0) {
                stunDuration -= deltaTime;
                if (stunDuration <= 0) {
                    state = CharacterState.IDLE;
                }
            }
            
            if (state == CharacterState.ATTACKING) {
                // Attack has duration
                state = CharacterState.IDLE;
            }
            
            if (state == CharacterState.SPRINTING) {
                stamina = Math.max(0, stamina - 1);
            }
            
            invulnerable = (state == CharacterState.DODGING);
        }
        
        void reset() {
            health = 100;
            stamina = 100;
            alive = true;
            state = CharacterState.IDLE;
            stunDuration = 0;
        }
    }
    
    static class CharacterStateMachine {
        private Player player;
        private CharacterState currentState = CharacterState.IDLE;
        
        CharacterStateMachine(Player player) {
            this.player = player;
        }
        
        CharacterState getCurrentState() { 
            return player.state; 
        }
        
        void transitionTo(CharacterState newState) {
            player.state = newState;
            if (player.animController != null) {
                player.animController.playAnimation(getAnimationName(newState));
            }
        }
        
        void update() {
            if (player.verticalVelocity < 0 && player.isAirborne()) {
                player.state = CharacterState.FALLING;
            }
        }
        
        private String getAnimationName(CharacterState state) {
            return state.name().toLowerCase();
        }
    }
    
    static class AnimationController {
        private String currentAnimation = "idle";
        
        void playAnimation(String name) {
            currentAnimation = name;
        }
        
        String getCurrentAnimation() {
            return currentAnimation;
        }
    }
}
