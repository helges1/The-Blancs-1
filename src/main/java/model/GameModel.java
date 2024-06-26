package model;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

import model.lasers.Laser;
import model.powerUps.PowerUps;
import model.powerUps.BasicPowerUpsFactory;
import model.powerUps.PowerUps.PowerUpType;
import model.powerUps.PowerUpsFactory;
import model.ships.BasicShipFactory;
import model.ships.Explosion;
import model.ships.Ship;
import model.ships.ShipFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the model of the game. This class contains all the game objects and
 * the logic for updating the game state.
 */
public class GameModel {

    // Initialize player ship
    private Ship playerShip;

    // Initialize user name and score
    private String userName;
    private int score;

    // Initialize lists with objects
    private LinkedList<Ship> enemyShips;
    private LinkedList<Laser> enemyLasers;
    private LinkedList<PowerUps> powerUps;
    private LinkedList<Asteroid> asteroids;
    private LinkedList<Laser> playerLasers;
    private LinkedList<Explosion> explosions;

    // World values for boundaries or other purposes
    public final static float WORLD_WIDTH = 800;
    public final static float WORLD_HEIGHT = 600;

    // Factories
    private ShipFactory shipFactory;
    private PowerUpsFactory powerUpsFactory;

    // Initialize timers
    private float timeSincePowerUpSpawn;
    private float timeSinceAsteroidSpawn;
    private float timeSinceEnemySpawn;

    // Keep track of destroyed enemy ships
    int destroyedEnemyShipsCount = 0;

    // Game over boolean
    private boolean gameOver = false;

    // Initialize level
    private GameLevel currentLevel;

    // Viewport
    private FitViewport viewport;

    // Textures
    private final TextureAtlas atlas;
    private final TextureRegion asteroidTexture;
    private final TextureRegion explosionTexture;

    // Sounds
    private final Sound laserSound;

    /**
     * A general constructor for the GameModel where everything is provided as
     * arguments (except world sizes, which are universal constants).
     *
     * @param atlas the TextureAtlas containing all the textures for the game.
     * @param laserSound a Sound to be played when lasers are fired.
     * @param viewport a FitViewport to make the the view and the sprites work with each other.
     * @param userName a String representing the username of the current player, used to track high score and such.
     */
    public GameModel(TextureAtlas atlas,
                     Sound laserSound, FitViewport viewport,
                     String userName) {


        this.atlas = atlas;
        this.asteroidTexture = atlas.findRegion("asteroid1");
        this.explosionTexture = atlas.findRegion("explosion");
        this.shipFactory = new BasicShipFactory(viewport, atlas);
        this.powerUpsFactory = new BasicPowerUpsFactory(atlas);

        // Initialize level
        this.currentLevel = GameLevel.LEVEL_1;

        // Initialize userName
        this.userName = userName;

        // Initialize sounds
        this.laserSound = laserSound;

        // Initialize player
        playerShip = shipFactory.getPlayerShip();
        playerLasers = new LinkedList<>();

        // Initialize enemies
        enemyShips = new LinkedList<>();
        enemyLasers = new LinkedList<>();
        powerUps = new LinkedList<>();
        asteroids = new LinkedList<>();
        explosions = new LinkedList<>();

        // Initialize power up spawn values
        this.timeSincePowerUpSpawn = 0;
        this.timeSinceEnemySpawn = 0;
        this.timeSinceAsteroidSpawn = 0;

        // Set the viewport
        this.viewport = viewport;
    }

    /**
     * Updates the game model based on the time passed since the last update.
     * @param deltaTime the time passed since the last update.
     */
    public void updateModel(float deltaTime) {
        // Update score
        updateScore();

        // Change level based on score
        changeLevel(score);

        // Update timers
        timeSincePowerUpSpawn += deltaTime;
        timeSinceEnemySpawn += deltaTime;
        timeSinceAsteroidSpawn += deltaTime;

        // Update playerShip
        playerShip.update(deltaTime);

        // Update enemyShips
        updateEnemyShips(enemyShips, deltaTime);

        // Update power ups
        updatePowerUps(deltaTime);

        // Update Asteroids
        updateAsteroids(deltaTime);

        // Update player and enemy lasers
        updateLasers(deltaTime);

        // Update explosions
        updateExplosions(deltaTime);

        // Spawn new EnemyShips
        if (timeSinceEnemySpawn >= currentLevel.getEnemySpawnRate() &&
                enemyShips.size() < currentLevel.getMaxEnemiesOnScreen()) {
            enemyShips.add(shipFactory.getEnemyShip(currentLevel));
            timeSinceEnemySpawn = 0;
        }

        // Spawn new PowerUps
        if (timeSincePowerUpSpawn >= currentLevel.getPowerUpSpawnRate()) {
            powerUps.add(powerUpsFactory.createPowerUp(currentLevel.getPowerUpSpawnRate()));
            timeSincePowerUpSpawn = 0;
        }

        // Spawn new Asteroids
        if (timeSinceAsteroidSpawn >= currentLevel.getAsteroidSpawnRate()) {
            spawnAsteroids();
            timeSinceAsteroidSpawn = 0;
        }
        
        updateGameState();
    }

    // Helper method to update ships
    private void updateEnemyShips(List<Ship> enemyShips, float deltaTime) {
        for (Ship enemyShip : enemyShips) {
            enemyShip.update(deltaTime);
        }
    }

    // Helper method to update score
    private void updateScore() {
        score += destroyedEnemyShipsCount * 10;
        resetDestroyedEnemyShipsCount();
    }

    // Helper method to update power-ups
    private void updatePowerUps(float deltaTime) {
        Iterator<PowerUps> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUps powerUp = powerUpIterator.next();
            powerUp.update(deltaTime);
            if (powerUp.isExpired()) {
                powerUpIterator.remove(); // Remove expired power ups
            } else {
                checkPowerUpCollision(powerUp);
            }
        }
    }

    // Helper method to update Asteroids
    private void updateAsteroids(float deltaTime) {
        Iterator<Asteroid> AsteroidIterator = asteroids.iterator();
        while (AsteroidIterator.hasNext()) {
            Asteroid Asteroid = AsteroidIterator.next();
            Asteroid.update(deltaTime);
            if (Asteroid.isOffScreen(WORLD_HEIGHT)) {
                AsteroidIterator.remove(); // Remove off-screen Asteroids
            } else if (checkAsteroidCollision(Asteroid)) {
            	AsteroidIterator.remove();
                asteroidExplosion(Asteroid);
            }
        }
    }

    private void updateExplosions(float deltaTime) {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                explosionIterator.remove(); // Remove finished explosions
            }
        }
    }

    // Helper method to update lasers
    private void updateLasers(float deltaTime) {
        // Update player lasers
        updatePlayerLasers(deltaTime);

        // Update enemy lasers
        Iterator<Laser> enemyLaserIterator = enemyLasers.iterator();
        while (enemyLaserIterator.hasNext()) {
            Laser laser = enemyLaserIterator.next();
            laser.update(deltaTime);

            if (laser.isOffScreen(WORLD_HEIGHT)) {
                enemyLaserIterator.remove(); // Remove off-screen lasers
            } else {
                // Check for collisions with player ship
                if (checkCollision(laser, playerShip)) {
                    playerShip.takeDamage(laser.getDamage());
                    if (playerShip.isDestroyed()) {
                        gameOver = true;
                    }
                    enemyLaserIterator.remove(); // Remove the laser after hitting the ship
                }

                // Check for collisions with asteroids (new code)
                Iterator<Asteroid> asteroidIterator = asteroids.iterator();
                while (asteroidIterator.hasNext()) {
                    Asteroid asteroid = asteroidIterator.next();
                    if (checkCollision(laser, asteroid)) {
                    	asteroidIterator.remove();
                        asteroidExplosion(asteroid);
                        enemyLaserIterator.remove(); // Remove the laser after hitting the asteroid
                        break; // Break to avoid ConcurrentModificationException
                    }
                }
            }
        }
    }

    // Helper method to update player lasers (extracted from updateLasers)
    private void updatePlayerLasers(float deltaTime) {
        Iterator<Laser> laserIterator = playerLasers.iterator();
        while (laserIterator.hasNext()) {
            Laser laser = laserIterator.next();
            laser.update(deltaTime);
            if (laser.isOffScreen(WORLD_HEIGHT)) {
                laserIterator.remove(); // Remove off-screen lasers
            } else {
                // Check for collisions with enemy ships
                Iterator<Ship> enemyShipIterator = enemyShips.iterator();
                while (enemyShipIterator.hasNext()) {
                    Ship enemyShip = enemyShipIterator.next();
                    if (checkCollision(laser, enemyShip)) {
                        enemyShip.takeDamage(laser.getDamage());
                        if (enemyShip.isDestroyed()) {
                        	enemyShipIterator.remove();
                            enemyShipExplosion(enemyShip);
                            destroyedEnemyShipsCount++; // Increment the count of destroyed enemy ships
                        }
                        laserIterator.remove(); // Remove the laser after hitting the ship
                        break; // Break to avoid ConcurrentModificationException
                    }
                }

                // Check for collisions with asteroids
                Iterator<Asteroid> asteroidIterator = asteroids.iterator();
                while (asteroidIterator.hasNext()) {
                    Asteroid asteroid = asteroidIterator.next();
                    if (checkCollision(laser, asteroid)) {
                    	asteroidIterator.remove();
                        asteroidExplosion(asteroid);
                        laserIterator.remove(); // Remove the laser after hitting the asteroid
                        break; // Break to avoid ConcurrentModificationException
                    }
                }
            }
        }
    }


    // Method to spawn a Asteroid
    private void spawnAsteroids() {
        // Randomly select a position for Asteroid
        float xPos = MathUtils.random(0 + 20, WORLD_WIDTH - 20);

        // Create random size from 50 to 150
        int asteroidSize = MathUtils.random(50, 150);
        // Creating the Asteroid
        Asteroid Asteroid = new Asteroid(asteroidTexture, xPos, new Vector2(0, -100), asteroidSize);

        // Adding the power up to the list
        asteroids.add(Asteroid);
    }

    // Helper method to check collision between power-up and player ship
    private void checkPowerUpCollision(PowerUps powerUp) {
        if (playerShip.getBoundingRectangle().overlaps(powerUp.getBoundingRectangle())) {
            powerUpCollected(powerUp);
        }
    }

    // Helper method to check collision between Asteroid and player ship
    private boolean checkAsteroidCollision(Asteroid Asteroid) {

        // Initialize collision detection to false
        boolean collisionDetected = false;

        // Calculate the centers of the player ship and the asteroid
        float playerCenterX = playerShip.getX() + playerShip.getWidth() / 2;
        float playerCenterY = playerShip.getY() + playerShip.getHeight() / 2;
        float AsteroidCenterX = Asteroid.getX() + Asteroid.getWidth() / 2;
        float AsteroidCenterY = Asteroid.getY() + Asteroid.getHeight() / 2;

        // Calculate the distance between the centers
        float distance = Vector2.dst(playerCenterX, playerCenterY, AsteroidCenterX, AsteroidCenterY);
        float sumRadii = playerShip.getWidth() / 2 + Asteroid.getWidth() / 2;

        // Check for collision with the player ship
        if (distance < sumRadii) {
            playerShip.takeDamage(10);
            collisionDetected = true;
            if (playerShip.isDestroyed()) {
                gameOver = true;
            }
        }

        // Check collisions with enemy ships
        Iterator<Ship> shipIterator = enemyShips.iterator();
        while (shipIterator.hasNext()) {
            Ship enemyShip = shipIterator.next();
            float enemyCenterX = enemyShip.getX() + enemyShip.getWidth() / 2;
            float enemyCenterY = enemyShip.getY() + enemyShip.getHeight() / 2;

            float distanceToEnemy = Vector2.dst(enemyCenterX, enemyCenterY, AsteroidCenterX, AsteroidCenterY);
            float sumRadiiEnemy = enemyShip.getWidth() / 2 + Asteroid.getWidth() / 2;

            if (distanceToEnemy < sumRadiiEnemy) {
                shipIterator.remove(); // Remove the enemy ship if hit by an asteroid
                collisionDetected = true;
            }
        }
        return collisionDetected;
    }

    // Helper method to change level based on score
    private void changeLevel(int score) {
        if (score >= 300 && score < 600) {
            currentLevel = GameLevel.LEVEL_2;
        } else if (score >= 600 && score < 900) {
            currentLevel = GameLevel.LEVEL_3;
        } else if (score >= 900) {
            currentLevel = GameLevel.LEVEL_4;
        }
    }

    // Helper method to handle power-up collection
    void powerUpCollected(PowerUps powerUp) {
        powerUps.remove(powerUp); // Remove the power-up after collecting
        switch (powerUp.getPowerUpType()) {
            case LIFE:
                playerShip.addHealth();
                break;
            case GUN:
                playerShip.setActivePowerUp(PowerUpType.GUN);
                break;
            case SHIELD:
                playerShip.setActivePowerUp(PowerUpType.SHIELD);
                break;
            case BLAST:
                playerShip.setActivePowerUp(PowerUpType.BLAST);
                break;
        }
        playerShip.resetPowerUpTimer();
    }

    /**
     * Method to fire a laser from the player ship
     */
    public void firePlayerLaser() {
        if (playerShip.getActivePowerUp() == PowerUpType.GUN) {
            // If the player ship's gun is upgraded, shoot bursts of lasers
            if (playerShip.fireLaser(playerLasers)) { // Pass playerLasers list
                laserSound.play();
                laserSound.play();
                laserSound.play();
            }
        } else {
            // If the gun is not upgraded, fire a single laser
            if (playerShip.fireLaser(playerLasers))
                laserSound.play();
        }
    }

    /**
     * Method to fire a laser from an enemy ship
     */
    public void fireEnemyLaser(Ship enemyShip) {
        if (enemyShip.fireLaser(enemyLasers))
            laserSound.play();
    }
    
    /**
     * Method to update the game state
     */
    private void updateGameState() {
    	Iterator<Ship> iterator = enemyShips.iterator();
        while (iterator.hasNext()) {
            Ship enemyShip = iterator.next();
            if (enemyShip.getBoundingRectangle().overlaps(playerShip.getBoundingRectangle())) {
                playerShip.takeDamage(10);
                enemyShip.takeDamage(10);
                if (enemyShip.isDestroyed()) {
                	iterator.remove();
                    enemyShipExplosion(enemyShip);
                }
                if (playerShip.isDestroyed()) {
                    gameOver = true;
                }
            }
        }
    }
    

    /**
     * Resets the number of destroyed enemy ships. 
     * Used to update the score without counting the same ship multiple times
     */
    private void resetDestroyedEnemyShipsCount() {
        destroyedEnemyShipsCount = 0;
    }

    /**
     * Gets the enemy ships list
     * 
     * @return The list of enemy ships
     */
    public LinkedList<Ship> getEnemyShips() {
        return enemyShips;
    }

    /**
     * Checks for collision between a laser and a ship
     * @param laser The laser object
     * @param ship The ship object
     * 
     * @return True if a collision is detected, false otherwise
     */
    private boolean checkCollision(Laser laser, Ship ship) {
        return laser.getBoundingRectangle().overlaps(ship.getBoundingRectangle());
    }

    /**
     * Checks for collision between a laser and an asteroid
     * @param laser The laser object
     * @param Asteroid The asteroid object
     * 
     * @return True if a collision is detected, false otherwise
     */
    private boolean checkCollision(Laser laser, Asteroid Asteroid) {
        // Calculate the center position of the asteroid
        float AsteroidCenterX = Asteroid.getX() + Asteroid.getWidth() / 2;
        float AsteroidCenterY = Asteroid.getY() + Asteroid.getHeight() / 2;

        // Calculate the radius of the asteroid
        float AsteroidRadius = Asteroid.getHeight() / 2;

        // Calculate the position of the laser
        float laserCenterX = laser.getX() + laser.getWidth() / 2;
        float laserCenterY = laser.getY() + laser.getHeight() / 2;

        // Calculate the distance between the centers of the laser and the asteroid
        float distance = Vector2.dst(AsteroidCenterX, AsteroidCenterY, laserCenterX, laserCenterY);

        // Check if the distance is close to the radius of the asteroid
        float threshold = laser.getWidth() / 2;

        // Check if the laser hits near the circumference of the asteroid
        return Math.abs(distance - AsteroidRadius) <= threshold;
    }

    /**
     * Getter method for the player ship
     * 
     * @return The player ship
     */
    public Ship getPlayerShip() {
        return playerShip;
    }

    /**
     * Getter method for viewport
     * 
     * @return The viewport
     */
    public FitViewport getViewport() {
        return viewport;
    }

    /**
     * Getter method for the player lasers list
     * 
     * @return The list of player lasers
     */
    public LinkedList<Laser> getPlayerLasers() {
        return playerLasers;
    }

    /**
     * Getter method for the enemy lasers list
     * 
     * @return The list of enemy lasers
     */
    public LinkedList<Laser> getEnemyLasers() {
        return enemyLasers;
    }

    /**
     * Getter method for the power-ups list
     * 
     * @return The list of power-ups
     */
    public LinkedList<PowerUps> getPowerUps() {
        return powerUps;
    }

    /**
     * Getter method for the asteroids list
     * 
     * @return The list of asteroids
     */
    public LinkedList<Asteroid> getAsteroids() {
        return asteroids;
    }

    /**
     * Getter method for the explosions list
     * 
     * @return The list of explosions
     */
    public LinkedList<Explosion> getExplosions() {
        return explosions;
    }

    /**
     * Getter method for the score
     * 
     * @return The username of the player
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Setter method for the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * checks if the game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Getter method for the current game level
     * 
     * @return The current game level
     */
    public GameLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Getter method for the score
     * 
     * @return The current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Getter method for the power-up texture
     * 
     * @param type The type of power-up
     * @return The texture region of the power-up
     */
    public TextureRegion getPowerUpTexture(PowerUpType type) {
        return atlas.findRegion(type.getTextureName());
    }
    
    public TextureRegion getAirBlastTexture() {
    	return atlas.findRegion("air-blast");
    }

    /**
     * Method to handle the explosion of an enemy ship
     *
     * @param enemyShip The enemy ship that exploded
     */
    void enemyShipExplosion(Ship enemyShip) {
        Explosion explosion = new Explosion(explosionTexture, enemyShip.getBoundingRectangle(), 0.5f);
        explosions.add(explosion);
    }

    /**
     * Method to handle the explosion of an asteroid
     *
     * @param asteroid The asteroid that exploded
     */
    void asteroidExplosion(Asteroid asteroid) {
        Explosion explosion = new Explosion(explosionTexture, asteroid.getBoundingRectangle(), 0.5f);
        explosions.add(explosion);
    }
    
    /**
     * Method to reset the game state
     * Used when the player restarts the game
     */
    public void resetGameState() {
        gameOver = false;
        score = 0;
        destroyedEnemyShipsCount = 0;
        playerLasers.clear();
        enemyLasers.clear();
        enemyShips.clear();
        powerUps.clear();
        asteroids.clear();
        explosions.clear();
        powerUps.clear();
        playerShip.setActivePowerUp(PowerUpType.NONE);
        playerShip.cooldownLaser(0.1f);
        playerShip.setPosition(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        playerShip.setHealth(100);
    }

	public TextureRegion getBackgroundTexture() {
		return atlas.findRegion("background");
	}

	public TextureRegion getShieldTexture() {
		return atlas.findRegion("shield");
	}


}