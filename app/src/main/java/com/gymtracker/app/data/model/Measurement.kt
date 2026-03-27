package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une mesure corporelle à une date donnée
 */
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val weight: Float? = null,           // Poids en kg
    val bodyFat: Float? = null,          // % de graisse corporelle
    val armLeft: Float? = null,          // Tour de bras gauche en cm
    val armRight: Float? = null,         // Tour de bras droit en cm
    val chest: Float? = null,            // Tour de poitrine en cm
    val waist: Float? = null,            // Tour de taille en cm
    val hips: Float? = null,             // Tour de hanches en cm
    val thighLeft: Float? = null,        // Tour de cuisse gauche en cm
    val thighRight: Float? = null,       // Tour de cuisse droit en cm
    val calfLeft: Float? = null,         // Tour de mollet gauche en cm
    val calfRight: Float? = null,        // Tour de mollet droit en cm
    val shoulders: Float? = null,        // Tour d'épaules en cm
    val neck: Float? = null,             // Tour de cou en cm
    val forearmLeft: Float? = null,      // Tour d'avant-bras gauche en cm
    val forearmRight: Float? = null,     // Tour d'avant-bras droit en cm
    val notes: String = ""               // Notes optionnelles
)

/**
 * Types de mesures disponibles pour l'affichage
 */
enum class MeasurementType(
    val displayName: String,
    val unit: String,
    val icon: String
) {
    WEIGHT("Poids", "kg", "⚖️"),
    BODY_FAT("Masse grasse", "%", "📊"),
    ARM_LEFT("Bras gauche", "cm", "💪"),
    ARM_RIGHT("Bras droit", "cm", "💪"),
    CHEST("Poitrine", "cm", "🫁"),
    WAIST("Taille", "cm", "📏"),
    HIPS("Hanches", "cm", "🍑"),
    THIGH_LEFT("Cuisse gauche", "cm", "🦵"),
    THIGH_RIGHT("Cuisse droite", "cm", "🦵"),
    CALF_LEFT("Mollet gauche", "cm", "🦶"),
    CALF_RIGHT("Mollet droit", "cm", "🦶"),
    SHOULDERS("Épaules", "cm", "🎯"),
    NECK("Cou", "cm", "👔"),
    FOREARM_LEFT("Avant-bras gauche", "cm", "💪"),
    FOREARM_RIGHT("Avant-bras droit", "cm", "💪")
}

