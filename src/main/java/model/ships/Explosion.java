package model.ships;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * This class is responsible for creating an explosion animation when an enemy ship is destroyed.
 */
public class Explosion {

    private Animation<TextureRegion> explosionAnimation;
    private float explosionTimer;

    private Rectangle boundingBox;

    private TextureRegion texture;
    /**
     * Create a new explosion animation.
     * @param explosionTexture
     * @param boundingBox
     * @param totalAnimationTime
     */
    public Explosion(TextureRegion explosionTexture, Rectangle boundingBox, float totalAnimationTime) {
        this.texture = explosionTexture;
        // Create a bounding box that is twice the size of the original bounding box
        this.boundingBox = boundingBox.setHeight(boundingBox.height * 2f).setWidth(boundingBox.width * 2f);
        
        this.explosionTimer = totalAnimationTime;

        TextureRegion[][] textureRegion2D = texture.split(128, 128);
        TextureRegion[] textureRegion1D = new TextureRegion[64];
        int index = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                textureRegion1D[index++] = textureRegion2D[i][j];
            }
        }
        explosionAnimation = new Animation<TextureRegion>(totalAnimationTime / 64, textureRegion1D);
        explosionTimer = 0;
    }

    /**
     * Progress the animation according to the amount of time that has passed.
     * @param deltaTime the amount of time passed in seconds.
     */
    public void update(float deltaTime) {
        explosionTimer += deltaTime;
    }
    /**
     * Draw the explosion animation.
     * @param batch The sprite batch to draw the explosion animation.
     */
    public void draw(SpriteBatch batch) {
        if (!explosionAnimation.isAnimationFinished(explosionTimer)) {
            batch.draw(explosionAnimation.getKeyFrame(explosionTimer), boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        }
    }
    /**
     * Check if the explosion animation has finished.
     * @return True if the explosion animation has finished, false otherwise.
     */
    public boolean isFinished() {
        return explosionAnimation.isAnimationFinished(explosionTimer);
    }

}
