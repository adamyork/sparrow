package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.enemy.MapEnemy
import com.github.adamyork.sparrow.game.engine.Particles
import com.github.adamyork.sparrow.game.engine.data.Particle
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import com.github.adamyork.sparrow.game.service.AssetService
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.awt.Color
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

class DefaultParticles : Particles {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultParticles::class.java)
        const val MAX_SQUARE_RADIAL_RADIUS: Int = 45
        const val MAX_ACTIVE_PROJECTILES: Int = 2
    }

    val dustParticleOffsets: HashMap<Int, Pair<Int, Int>> = HashMap()
    val colorMap: HashMap<ParticleType, Color>

    constructor(assetService: AssetService) {
        colorMap = parseParticleColorMap(assetService.applicationYamlFile)
        dustParticleOffsets[0] = Pair(8, 0)
        dustParticleOffsets[1] = Pair(1, 2)
        dustParticleOffsets[2] = Pair(10, 4)
        dustParticleOffsets[3] = Pair(20, 6)
        dustParticleOffsets[4] = Pair(36, 8)
        dustParticleOffsets[5] = Pair(44, 8)
        dustParticleOffsets[7] = Pair(40, 6)
        dustParticleOffsets[8] = Pair(35, 4)
        dustParticleOffsets[9] = Pair(21, 4)
        dustParticleOffsets[10] = Pair(19, 2)
        dustParticleOffsets[11] = Pair(29, 0)
    }

    override fun createCollisionParticles(originX: Int, originY: Int): ArrayList<Particle> {
        val intRange = 0..360
        return intRange.toList().toIntArray().map {
            Particle(
                it,
                originX,
                originY,
                originX,
                originY,
                2,
                2,
                ParticleType.COLLISION,
                0,
                20,
                Random.nextInt(50),
                Random.nextInt(50),
                1,
                colorMap[ParticleType.COLLISION] ?: Color.WHITE
            )
        }.toCollection(ArrayList())
    }

    override fun createDustParticles(player: Player): ArrayList<Particle> {
        var startX: Int
        var startY = player.y + player.height - (player.height / 16)
        val intRange = 0..11
        return intRange.toList().toIntArray().map {
            if (player.direction == Direction.LEFT) {
                startX = player.x + player.width - (player.width / 3)
                startX += dustParticleOffsets[it]?.first ?: 0
            } else {
                startX = player.x + (player.width / 5)
                startX -= dustParticleOffsets[it]?.first ?: 0
            }
            startY -= dustParticleOffsets[it]?.second ?: 0
            val color = colorMap[ParticleType.DUST] ?: Color.WHITE
            val adjustedAlphaColor = Color(color.red, color.green, color.blue, color.alpha - (it * 23))
            Particle(
                it,
                startX,
                startY,
                player.x,
                player.y,
                (it * 3).coerceAtMost(30),
                (it * 3).coerceAtMost(30),
                ParticleType.DUST,
                0,
                5,
                0,
                0,
                0,
                adjustedAlphaColor
            )
        }.toCollection(ArrayList())
    }

    override fun createProjectileParticle(
        player: Player,
        enemy: MapEnemy,
        particles: ArrayList<Particle>
    ): Tuple2<ArrayList<Particle>, Boolean> {
        val count = particles.filter { it.type == ParticleType.FURBALL }
            .size
        val particles: ArrayList<Particle> = ArrayList()
        var particleAdded = false
        if (count < MAX_ACTIVE_PROJECTILES) {
            val xDiff = abs(enemy.x - player.x)
            val yDiff = abs(enemy.y - player.y)
            val xIncrement = (xDiff / 10).coerceAtLeast(1)
            val yIncrement = (yDiff / 10).coerceAtLeast(1)
            particleAdded = true
            particles.add(
                Particle(
                    count + 1,
                    enemy.x,
                    enemy.y,
                    player.x,
                    player.y,
                    24,
                    24,
                    ParticleType.FURBALL,
                    0,
                    10,
                    xIncrement,
                    yIncrement,
                    1,
                    colorMap[ParticleType.FURBALL] ?: Color.WHITE
                )
            )
        }
        return Tuples.of(particles, particleAdded)
    }

    private fun parseParticleColorMap(file: File): HashMap<ParticleType, Color> {
        val colorMap: HashMap<ParticleType, Color> = HashMap()
        val yamlDefault = Yaml.Default
        val properties = yamlDefault.decodeFromString(YamlMap.serializer(), file.readText())
        val map = properties["particle"] as YamlMap
        val player = map["player"] as YamlMap
        val enemy = map["enemy"] as YamlMap
        val playerMovement = player["movement"] as YamlMap
        val playerCollision = player["collision"] as YamlMap
        val enemyProjectile = enemy["projectile"] as YamlMap
        val playerMovementParticleColorMap = playerMovement["color"] as YamlMap
        val playerCollisionParticleColorMap = playerCollision["color"] as YamlMap
        val enemyProjectileParticleColorMap = enemyProjectile["color"] as YamlMap
        val playerMovementParticleColor = Color(
            playerMovementParticleColorMap["r"]?.content.toString().toInt(),
            playerMovementParticleColorMap["g"]?.content.toString().toInt(),
            playerMovementParticleColorMap["b"]?.content.toString().toInt(),
            playerMovementParticleColorMap["a"]?.content.toString().toInt()
        )
        val playerCollisionParticleColor = Color(
            playerCollisionParticleColorMap["r"]?.content.toString().toInt(),
            playerCollisionParticleColorMap["g"]?.content.toString().toInt(),
            playerCollisionParticleColorMap["b"]?.content.toString().toInt(),
            playerCollisionParticleColorMap["a"]?.content.toString().toInt()
        )
        val enemyProjectileParticleColor = Color(
            enemyProjectileParticleColorMap["r"]?.content.toString().toInt(),
            enemyProjectileParticleColorMap["g"]?.content.toString().toInt(),
            enemyProjectileParticleColorMap["b"]?.content.toString().toInt(),
            enemyProjectileParticleColorMap["a"]?.content.toString().toInt()
        )
        colorMap[ParticleType.DUST] = playerMovementParticleColor
        colorMap[ParticleType.COLLISION] = playerCollisionParticleColor
        colorMap[ParticleType.FURBALL] = enemyProjectileParticleColor
        return colorMap
    }

}