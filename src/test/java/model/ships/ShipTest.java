package model.ships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import model.lasers.Laser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.mockito.Mockito;


public class ShipTest {

    private Ship ship;
    private Texture texture;
    private float xPos = 250;
    private float yPos = 250;
    private float width = 50;
    private float height = 50;
    private float speed = 200;
    private float health = 100;

   
    @BeforeEach
    public void setUp() {
        texture = Mockito.mock(Texture.class);
        Mockito.when(texture.getWidth()).thenReturn((int) width);
        Mockito.when(texture.getHeight()).thenReturn((int) height);
    
        TextureRegion textureRegion = new TextureRegion(texture);
        Viewport testViewport = new FitViewport(800, 600); // Or mock this as well
    
        // Create a Ship instance for testing
        ship = new Ship(textureRegion, xPos, yPos, width, height, speed, health, testViewport) {
            @Override
            public void update(float delta) {
                // Do nothing for now
            }
    
            @Override
            public boolean fireLaser(List<Laser> lasers) {
                return true; // Do nothing for now
            }

			@Override
			void resetCannon() {
				// Do nothing for now
				
			}

			@Override
			void upgradeCannon() {
				// Do nothing for now 
			}
        };
    }
    
     
    @Test
    public void testMoveUp() {
        float initialX = ship.getY();
        ship.moveUp(1); // Move the ship up for 1 second
        assertEquals(initialX + ship.speed , ship.getY(), 0.1);
    }
    @Test
    public void testMoveDown() {
        float initialY = ship.getY();
        ship.moveDown(1); // Move the ship down for 1 second
        assertEquals(initialY - ship.speed , ship.getY(), 0.1);
    }

    @Test
    public void testMoveLeft() {
        float initialX = ship.getX();
        ship.moveLeft(1); // Move the ship left for 1 second
        assertEquals(initialX - ship.speed , ship.getX(), 0.1);
    }
    
    @Test
    public void testMoveRight() {
        ship.setPosition(100, 100);
        float initialX = ship.getX();
        ship.moveRight(1); // Move the ship right for 1 second
        assertEquals(initialX + ship.speed, ship.getX(), 0.1);
    } 
    
    @Test
    public void testHealthManagement() {
        ship.takeDamage(50);
        assertEquals(50, ship.getHealth(), "Health should decrease by the damage amount.");

        ship.addHealth();
        assertEquals(70, ship.getHealth(), "Health should increase by 20.");
    }
    
    @Test
    public void testBoundaryMovement() {
        ship.setPosition(0, 0);
        ship.moveLeft(1);
        assertEquals(0, ship.getX(), "Ship should not move left past the left boundary.");

        ship.setPosition(800 - ship.getWidth(), 600 - ship.getHeight());
        ship.moveRight(1);
        assertEquals(800 - ship.getWidth(), ship.getX(), "Ship should not move right past the right boundary.");

        ship.setPosition(0, 0);
        ship.moveDown(1);
        assertEquals(0, ship.getY(), "Ship should not move down past the bottom boundary.");

        ship.setPosition(800 - ship.getWidth(), 600 - ship.getHeight());
        ship.moveUp(1);
        assertEquals(600 - ship.getHeight(), ship.getY(), "Ship should not move up past the top boundary.");
    }

    
    @Test
    public void testShipCreation() {
        assertNotNull(ship, "Ship should not be null.");
        assertEquals(xPos, ship.getX(), "X position should be set correctly.");
        assertEquals(yPos, ship.getY(), "Y position should be set correctly.");
        assertEquals(width, ship.getWidth(), "Width should be set correctly.");
        assertEquals(height, ship.getHeight(), "Height should be set correctly.");
        assertEquals(speed, ship.speed, "Speed should be set correctly.");
        assertEquals(health, ship.getHealth(), "Health should be set correctly.");
    }
    
    @Test
    public void testGetNosePosition0Degrees() {
    	Vector2 exptected = new Vector2(xPos + (width / 2), yPos + height);
    	Vector2 actual = ship.getNosePositionOfShip();
    	assertEquals(exptected, actual);
    }
    
    @Test
    public void testGetNosePosition90Degrees() {
    	ship.rotateShip(90);
    	Vector2 exptected = new Vector2(xPos, yPos + (width / 2));
    	Vector2 actual = ship.getNosePositionOfShip();
    	assertEquals(exptected, actual);
    }
    
    @Test
    public void testGetNosePosition180Degrees() {
    	ship.rotateShip(180);
    	Vector2 exptected = new Vector2(xPos + (width / 2), yPos);
    	Vector2 actual = ship.getNosePositionOfShip();
    	assertEquals(exptected, actual);
    }
    
    @Test
    public void testGetNosePosition270Degrees() {
    	ship.rotateShip(270);
    	Vector2 exptected = new Vector2(xPos + height, yPos + (width / 2));
    	Vector2 actual = ship.getNosePositionOfShip();
    	assertEquals(exptected, actual);
    }
    
    @Test
    public void testGetNosePositionMinus90Degrees() {
    	ship.rotateShip(-90);
    	Vector2 exptected = new Vector2(xPos + height, yPos + (width / 2));
    	Vector2 actual = ship.getNosePositionOfShip();
    	assertEquals(exptected, actual);
    }
    
    @Test
    public void testGetNosePosition45Degrees() {
    	ship.rotateShip(45);
    	float radians = (float) Math.toRadians(45);
    	float exptectedX = xPos + (width / 2)*MathUtils.cos(radians);
    	float expectedY = yPos + height*MathUtils.cos(radians) + (width / 2)*MathUtils.sin(radians);
    	Vector2 exptected = new Vector2(exptectedX, expectedY);
    	Vector2 actual = ship.getNosePositionOfShip();
    	// Check that the difference between expected and actual output is > 10^-10
    	double aproxZero = Math.pow(10, -10);
    	assertTrue(actual.x - exptected.x < aproxZero);
    	assertTrue(actual.y - exptected.y < aproxZero);
    }
    
    @Test
    public void testGetNosePosition187Degrees() {
    	ship.rotateShip(187);
    	float radians = (float) Math.toRadians(187);
    	float exptectedX = xPos + (width / 2)*(-MathUtils.cos(radians)) + height*(-MathUtils.sin(radians));
    	float expectedY = yPos + (width / 2)*(-MathUtils.sin(radians));
    	Vector2 exptected = new Vector2(exptectedX, expectedY);
    	Vector2 actual = ship.getNosePositionOfShip();
    	// Check that the difference between expected and actual output is > 10^-10
    	double aproxZero = Math.pow(10, -10);
    	assertTrue(actual.x - exptected.x < aproxZero);
    	assertTrue(actual.y - exptected.y < aproxZero);
    }


    @Test
    public void testPositionAfterRotation() {
    	assertEquals(250, ship.getX());
    	assertEquals(250, ship.getY());
    	ship.rotateShip(90);
    	assertEquals(250, ship.getX());
    	assertEquals(250, ship.getY());
    	ship.rotateShip(73);
    	assertEquals(250, ship.getX());
    	assertEquals(250, ship.getY());
    }



}
    
