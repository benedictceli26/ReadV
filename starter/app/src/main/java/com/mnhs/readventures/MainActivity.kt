package com.mnhs.readventures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.AuthRepository
import com.mnhs.readventures.data.ProgressStore
import com.mnhs.readventures.data.ReadingRepository
import com.mnhs.readventures.data.SampleData
import com.mnhs.readventures.ui.components.BottomNavItems
import com.mnhs.readventures.ui.components.ReadVenturesBottomNav
import com.mnhs.readventures.ui.screens.ChangePasswordScreen
import com.mnhs.readventures.ui.screens.CreateClassScreen
import com.mnhs.readventures.ui.screens.CreateMaterialQuizScreen
import com.mnhs.readventures.ui.screens.DictionaryScreen
import com.mnhs.readventures.ui.screens.EditProfileScreen
import com.mnhs.readventures.ui.screens.HomeScreen
import com.mnhs.readventures.ui.screens.JoinClassScreen
import com.mnhs.readventures.ui.screens.LevelsScreen
import com.mnhs.readventures.ui.screens.LibraryScreen
import com.mnhs.readventures.ui.screens.LoginScreen
import com.mnhs.readventures.ui.screens.MyClassScreen
import com.mnhs.readventures.ui.screens.ProfileScreen
import com.mnhs.readventures.ui.screens.ProgressScreen
import com.mnhs.readventures.ui.screens.QuizResultScreen
import com.mnhs.readventures.ui.screens.QuizScreen
import com.mnhs.readventures.ui.screens.ReadingScreen
import com.mnhs.readventures.ui.screens.TeacherDashboardScreen
import com.mnhs.readventures.ui.theme.ReadVenturesTheme

/**
 * Entry point — ReadVentures (student & teacher app).
 *
 * Navigation: Login -> Home -> Library -> Reading -> Quiz -> Quiz Result,
 * with a bottom nav on the four main tabs: Home / Library / Results / Profile.
 * Supports Dictionary, Level Progression & Gating, Class Mode & Codes,
 * and Teacher Dashboard for creating custom reading materials and quizzes.
 */
class MainActivity : ComponentActivity() {

    private lateinit var progressStore: ProgressStore
    private lateinit var authRepository: AuthRepository
    private lateinit var readingRepository: ReadingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressStore = ProgressStore(applicationContext)
        authRepository = AuthRepository(progressStore)
        readingRepository = ReadingRepository(progressStore)
        progressStore.loadIntoAppState()

        setContent {
            ReadVenturesTheme {
                ReadVenturesApp(
                    authRepository = authRepository,
                    readingRepository = readingRepository,
                    progressStore = progressStore
                )
            }
        }
    }
}

@Composable
fun ReadVenturesApp(
    authRepository: AuthRepository,
    readingRepository: ReadingRepository,
    progressStore: ProgressStore
) {
    val navController = rememberNavController()
    val materials = AppState.materials
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    // Load sample data immediately, then try Firestore in the background.
    LaunchedEffect(Unit) {
        readingRepository.loadMaterials { loaded ->
            for (m in loaded) {
                if (AppState.materials.none { it.id == m.id }) {
                    AppState.materials.add(m)
                }
            }
        }
        readingRepository.loadDictionaryWords { loaded ->
            for (w in loaded) {
                if (AppState.dictionaryWords.none { it.word.equals(w.word, ignoreCase = true) && it.materialId == w.materialId }) {
                    AppState.dictionaryWords.add(w)
                }
            }
        }
    }

    val isTeacher = AppState.currentUser.value.role == "teacher"
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mainRoutes = BottomNavItems.map { it.route }
    val showBottomNav = currentRoute in mainRoutes || (isTeacher && currentRoute == "teacher_dashboard")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomNav) {
                val navRoute = if (currentRoute == "teacher_dashboard") "home" else currentRoute
                ReadVenturesBottomNav(currentRoute = navRoute) { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        val startDest = when {
            !AppState.isLoggedIn.value -> "login"
            AppState.currentUser.value.role == "teacher" -> "teacher_dashboard"
            else -> "home"
        }

        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(authRepository = authRepository) {
                    val dest = if (AppState.currentUser.value.role == "teacher") "teacher_dashboard" else "home"
                    navController.navigate(dest) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            composable("home") {
                if (isTeacher) {
                    TeacherDashboardScreen(
                        showBack = false,
                        onBack = {},
                        onProfileClick = { navController.navigate("profile") },
                        onCreateClass = { navController.navigate("create_class") },
                        onCreateMaterial = { navController.navigate("create_material") },
                        onEditMaterial = { id -> navController.navigate("create_material?materialId=$id") }
                    )
                } else {
                    HomeScreen(
                        onOpenLibrary = { navController.navigate("library") },
                        onOpenReading = { id -> navController.navigate("reading/$id") },
                        onOpenQuiz = { id -> navController.navigate("quiz/$id") },
                        onOpenProgress = { navController.navigate("progress") },
                        onOpenProfile = { navController.navigate("profile") },
                        onOpenCategory = { category ->
                            categoryFilter = category
                            navController.navigate("library")
                        },
                        onOpenDictionary = { navController.navigate("dictionary") },
                        onOpenLevels = { navController.navigate("levels") },
                        onOpenMyClass = { navController.navigate("my_class") },
                        onJoinClass = { navController.navigate("join_class") },
                        onTeacherDashboard = { navController.navigate("teacher_dashboard") }
                    )
                }
            }

            composable("library") {
                LibraryScreen(
                    materials = materials,
                    initialCategory = categoryFilter,
                    onOpenReading = { id -> navController.navigate("reading/$id") },
                    onOpenProfile = { navController.navigate("profile") }
                )
            }

            composable(
                route = "reading/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("materialId") ?: materials.first().id
                val material = materials.firstOrNull { it.id == id } ?: materials.first()
                ReadingScreen(
                    material = material,
                    progressStore = progressStore,
                    onBack = { navController.popBackStack() },
                    onStartQuiz = { quizId -> navController.navigate("quiz/$quizId") }
                )
            }

            composable(
                route = "quiz/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("materialId") ?: materials.first().id
                val material = materials.firstOrNull { it.id == id } ?: materials.first()
                var questions by remember(id) {
                    mutableStateOf(AppState.questions.filter { it.materialId == id }.ifEmpty { SampleData.questionsFor(id) })
                }
                LaunchedEffect(id) {
                    readingRepository.loadQuestions(id) { loaded ->
                        if (loaded.isNotEmpty()) questions = loaded
                    }
                }
                QuizScreen(
                    material = material,
                    questions = questions,
                    onBack = { navController.popBackStack() },
                    onFinished = { materialId, score, total ->
                        val resultScore = com.mnhs.readventures.data.Score(
                            id = "$materialId-${System.currentTimeMillis()}",
                            userId = AppState.currentUser.value.uid,
                            materialId = materialId,
                            materialTitle = material.title,
                            score = score,
                            total = total,
                            date = System.currentTimeMillis()
                        )
                        readingRepository.saveScore(resultScore)
                        navController.navigate("result/$materialId/$score/$total") {
                            popUpTo("quiz/$materialId") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "result/{materialId}/{score}/{total}",
                arguments = listOf(
                    navArgument("materialId") { type = NavType.StringType },
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType }
                )
            ) { entry ->
                val id = entry.arguments?.getString("materialId") ?: materials.first().id
                val score = entry.arguments?.getInt("score") ?: 0
                val total = entry.arguments?.getInt("total") ?: 5
                val material = materials.firstOrNull { it.id == id } ?: materials.first()
                QuizResultScreen(
                    material = material,
                    score = score,
                    total = total,
                    onRetake = { navController.navigate("quiz/$id") },
                    onBackToLibrary = { navController.navigate("library") },
                    onHome = { navController.navigate("home") }
                )
            }

            composable("progress") {
                ProgressScreen(onOpenProfile = { navController.navigate("profile") })
            }

            composable("profile") {
                ProfileScreen(
                    onEditProfile = { navController.navigate("edit_profile") },
                    onChangePassword = { navController.navigate("change_password") },
                    onOpenProgress = { navController.navigate("progress") },
                    onLogOut = {
                        authRepository.signOut()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onOpenDictionary = { navController.navigate("dictionary") },
                    onOpenLevels = { navController.navigate("levels") },
                    onOpenMyClass = { navController.navigate("my_class") },
                    onTeacherDashboard = { navController.navigate("teacher_dashboard") }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    progressStore = progressStore,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable("change_password") {
                ChangePasswordScreen(
                    authRepository = authRepository,
                    onBack = { navController.popBackStack() }
                )
            }

            // --- FEATURE 1: Dictionary Screen ---
            composable("dictionary") {
                DictionaryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenReading = { id -> navController.navigate("reading/$id") }
                )
            }

            // --- FEATURE 2: Levels Screen ---
            composable("levels") {
                LevelsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenReading = { id -> navController.navigate("reading/$id") }
                )
            }

            // --- FEATURE 3: My Class & Join Class ---
            composable("my_class") {
                MyClassScreen(
                    onBack = { navController.popBackStack() },
                    onJoinClass = { navController.navigate("join_class") },
                    onOpenReading = { id -> navController.navigate("reading/$id") }
                )
            }

            composable("join_class") {
                JoinClassScreen(
                    onBack = { navController.popBackStack() },
                    onClassJoined = { navController.popBackStack() }
                )
            }

            // --- FEATURE 4: Teacher Dashboard, Create Class, Create Reading & Quiz ---
            composable("teacher_dashboard") {
                TeacherDashboardScreen(
                    showBack = false,
                    onBack = {},
                    onProfileClick = { navController.navigate("profile") },
                    onCreateClass = { navController.navigate("create_class") },
                    onCreateMaterial = { navController.navigate("create_material") },
                    onEditMaterial = { id -> navController.navigate("create_material?materialId=$id") }
                )
            }

            composable("create_class") {
                CreateClassScreen(
                    onBack = { navController.popBackStack() },
                    onClassCreated = { navController.popBackStack() }
                )
            }

            composable(
                route = "create_material?materialId={materialId}",
                arguments = listOf(
                    navArgument("materialId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                val editId = entry.arguments?.getString("materialId")
                CreateMaterialQuizScreen(
                    editingMaterialId = editId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable("create_material") {
                CreateMaterialQuizScreen(
                    editingMaterialId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
