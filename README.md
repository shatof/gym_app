# Gym Tracker - Application de suivi de musculation

## 📱 Description

Gym Tracker est une application Android simple et intuitive pour suivre vos séances de musculation. Elle permet d'enregistrer facilement vos exercices, séries, répétitions et poids utilisés directement pendant l'entraînement.

## ✨ Fonctionnalités

### Séance en cours
- **Démarrage rapide** : Un clic pour commencer une nouvelle séance
- **Chronomètre** : Suivi automatique de la durée de la séance
- **Ajout d'exercices** : Sélection rapide parmi les exercices courants ou saisie personnalisée
- **Autocomplétion** : Suggestions basées sur vos exercices précédents
- **Enregistrement des séries** :
  - Poids (avec boutons +/- pour ajustement rapide par pas de 2.5kg)
  - Répétitions (avec boutons +/-)
  - Miorep (optionnel)
- **Validation par checkbox** : Cochez chaque série une fois terminée
- **Copie automatique** : Les nouvelles séries reprennent les valeurs de la série précédente

### Historique
- **Liste des séances** : Visualisation de toutes vos séances passées
- **Détails** : Exercices, séries, poids maximaux
- **Statistiques** : Nombre d'exercices, séries, durée
- **Suppression** : Possibilité de supprimer les anciennes séances

### Progression (Graphiques)
- **Sélection d'exercice** : Choisissez un exercice pour voir sa progression
- **Graphique du poids maximum** : Évolution de votre charge maximale
- **Graphique du volume total** : Poids × Reps × Séries
- **Graphique du 1RM estimé** : Répétition maximale estimée (formule d'Epley)
- **Historique détaillé** : Toutes les performances pour l'exercice sélectionné

### Export
- **Format JSON** : Export complet de toutes vos données
- **Partage** : Envoi par email, cloud, etc.
- **Sauvegarde** : Conservation de vos données sur le long terme

## 🛠️ Installation

### Prérequis
- **Android Studio** Hedgehog (2023.1.1) ou plus récent
- **JDK 17** ou plus récent
- **Android SDK** avec API 34 (Android 14)
- Un téléphone Android avec **Android 8.0 (API 26)** minimum ou un émulateur

### Étapes d'installation

#### 1. Cloner/Ouvrir le projet
```bash
cd c:\Users\GoronLT\Documents\app_gym
```
Ouvrez ce dossier dans Android Studio.

#### 2. Synchroniser le projet
- Android Studio devrait automatiquement détecter le projet Gradle
- Cliquez sur "Sync Now" si demandé
- Attendez que toutes les dépendances soient téléchargées

#### 3. Configurer un appareil
**Option A - Téléphone physique (recommandé) :**
1. Sur votre téléphone, allez dans Paramètres > À propos du téléphone
2. Tapez 7 fois sur "Numéro de build" pour activer les options développeur
3. Retournez dans Paramètres > Options développeur
4. Activez "Débogage USB"
5. Branchez votre téléphone en USB à l'ordinateur
6. Acceptez l'autorisation de débogage sur votre téléphone

**Option B - Émulateur :**
1. Dans Android Studio, ouvrez Device Manager (icône téléphone à droite)
2. Cliquez sur "Create device"
3. Sélectionnez un modèle (ex: Pixel 6)
4. Téléchargez une image système (API 34 recommandé)
5. Terminez la création et lancez l'émulateur

#### 4. Compiler et installer
1. Sélectionnez votre appareil dans la liste déroulante en haut
2. Cliquez sur le bouton ▶️ "Run" (ou Shift+F10)
3. Attendez la compilation et l'installation
4. L'application se lance automatiquement !

### Générer un APK pour installation manuelle

Pour installer l'app sur un téléphone sans Android Studio :

1. Dans Android Studio : **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Attendez la fin de la compilation
3. Cliquez sur "locate" dans la notification
4. Le fichier `app-debug.apk` se trouve dans `app/build/outputs/apk/debug/`
5. Transférez ce fichier sur votre téléphone (USB, email, cloud...)
6. Sur le téléphone, ouvrez le fichier APK
7. Autorisez l'installation depuis des sources inconnues si demandé
8. Installez l'application

### APK de production (signé)

Pour une version optimisée :
1. **Build > Generate Signed Bundle / APK**
2. Créez une nouvelle keystore ou utilisez une existante
3. Choisissez "APK"
4. Sélectionnez "release"
5. L'APK signé sera généré dans `app/release/`

## 📖 Guide d'utilisation

### Démarrer une séance
1. Ouvrez l'application
2. Appuyez sur **"Commencer la séance"**
3. Le chronomètre démarre automatiquement

### Ajouter un exercice
1. Appuyez sur **"Ajouter un exercice"**
2. Sélectionnez un exercice courant ou tapez le nom
3. Une première série est automatiquement créée

### Enregistrer une série
1. Ajustez le **poids** avec les boutons +/- (pas de 2.5kg)
2. Ajustez les **répétitions** avec les boutons +/-
3. Ajoutez le **miorep** si nécessaire (optionnel)
4. **Cochez la case** une fois la série terminée
5. Appuyez sur **"+ Série"** pour ajouter une nouvelle série

### Terminer la séance
1. Appuyez sur le bouton vert **"Terminer"**
2. Confirmez dans la popup
3. La séance est sauvegardée avec sa durée

### Voir la progression
1. Allez dans l'onglet **"Progression"**
2. Sélectionnez un exercice
3. Consultez les graphiques et statistiques

### Exporter les données
1. Appuyez sur l'icône de partage 📤
2. Choisissez où envoyer le fichier JSON
3. Conservez ce fichier comme backup

## 🏗️ Architecture technique

```
com.gymtracker.app/
├── data/
│   ├── model/          # Entités (Workout, Exercise, ExerciseSet)
│   ├── dao/            # Interfaces Room (base de données)
│   ├── repository/     # Couche d'accès aux données
│   └── GymDatabase.kt  # Configuration Room
├── ui/
│   ├── components/     # Composants réutilisables
│   ├── navigation/     # Navigation entre écrans
│   ├── screens/        # Écrans principaux
│   ├── theme/          # Thème et couleurs
│   └── viewmodel/      # ViewModels
├── GymTrackerApp.kt    # Application
└── MainActivity.kt     # Point d'entrée
```

### Technologies utilisées
- **Kotlin** : Langage principal
- **Jetpack Compose** : Interface utilisateur moderne
- **Room** : Base de données locale SQLite
- **Vico** : Bibliothèque de graphiques
- **Material 3** : Design system
- **Coroutines & Flow** : Programmation asynchrone

## 📝 Format d'export JSON

```json
{
  "exportDate": 1706000000000,
  "appVersion": "1.0",
  "workouts": [
    {
      "id": 1,
      "date": 1705900000000,
      "name": "",
      "notes": "",
      "durationMinutes": 45,
      "isCompleted": true,
      "exercises": [
        {
          "id": 1,
          "name": "Développé couché",
          "orderIndex": 0,
          "sets": [
            {
              "setNumber": 1,
              "reps": 10,
              "weight": 60.0,
              "miorep": null,
              "isCompleted": true,
              "timestamp": 1705900100000
            }
          ]
        }
      ]
    }
  ]
}
```

## 🐛 Résolution de problèmes

### L'application ne compile pas
- Vérifiez que vous avez JDK 17+
- Synchronisez le projet Gradle (File > Sync Project with Gradle Files)
- Invalidez les caches (File > Invalidate Caches / Restart)

### L'appareil n'est pas détecté
- Vérifiez que le débogage USB est activé
- Essayez un autre câble USB
- Installez les drivers USB du fabricant

### Erreur "SDK not found"
- File > Project Structure > SDK Location
- Configurez le chemin vers votre Android SDK

## 📄 Licence

Ce projet est libre d'utilisation pour un usage personnel.

---

**Bon entraînement ! 💪**
