# TimeGuard (Android / Java)

TimeGuard est une application Android légère (Java + XML) qui surveille le temps passé sur certaines applications choisies par l’utilisateur et envoie une notification locale quand une limite est dépassée.

## Prérequis
- Android Studio compatible avec **AGP 8.9.1** (Gradle wrapper inclus)
- Android SDK installé (minSdk 29, target/compileSdk **35** par défaut)

## Compiler / lancer
1. Ouvrir ce dossier dans Android Studio.
2. Laisser Gradle synchroniser et télécharger les dépendances.
3. Lancer sur un appareil ou un émulateur Android 10+ (API 29+).

## Installer l’application
- Via Android Studio (Run) ou en générant un APK Debug.

## Accorder l’accès aux statistiques d’utilisation
Cette autorisation est **spéciale** (non demandée en popup).
1. Ouvrir TimeGuard.
2. Appuyer sur **“Accorder l’accès aux stats d’utilisation”**.
3. Dans l’écran système, choisir **TimeGuard** puis activer l’accès.

## Activer les notifications
- Android 13+ : TimeGuard doit obtenir la permission **Notifications** (bouton dédié sur l’écran principal).
- Sinon : vérifier que les notifications ne sont pas bloquées dans les paramètres de l’application.

## Tester rapidement (Instagram ou autre)
1. Installer/avoir une app cible (ex: Instagram).
2. Dans TimeGuard, **Sélectionner une application** puis définir une limite courte (ex: 1 minute).
3. Activer la règle, activer **Surveillance active**.
4. Ouvrir l’app cible et rester dessus.

## Limites importantes (Android)
- `WorkManager` en mode périodique est **imprécis** : l’intervalle minimum est **15 minutes** et Android peut retarder les exécutions (Doze, optimisation batterie, etc.).
- Pour être plus réactif, TimeGuard utilise aussi un **OneTimeWorker replanifié** (best-effort). Android peut malgré tout retarder en arrière-plan.
- La détection “app au premier plan” via `UsageStatsManager` dépend de la qualité des événements fournis par Android et peut être retardée ou incomplète selon l’appareil.
- Le résultat n’est **pas garanti à la seconde près**.

## Pistes d’amélioration
- Ajouter une option “mode précision” (ex: service au premier plan) avec un coût batterie explicite.
- Ajouter des exceptions (plages horaires, jours, etc.).
- Exporter l’historique, ou ajouter des statistiques hebdomadaires.
