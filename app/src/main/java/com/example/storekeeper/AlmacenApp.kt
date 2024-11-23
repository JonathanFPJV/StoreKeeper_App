package com.example.storekeeper

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.unpackInt1
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.storekeeper.ui.theme.Productos.AddProductScreen
import com.example.storekeeper.ui.theme.Productos.EditProductScreen
import com.example.storekeeper.ui.theme.Productos.ProductApiService
import com.example.storekeeper.ui.theme.Productos.ProductosListado
import org.tensorflow.lite.support.label.Category
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun AlmacenApp() {
    val urlBase = "https://api-of-administration.onrender.com/" // o tu IP si usarás un dispositivo externo
    val retrofit = Retrofit.Builder().baseUrl(urlBase)
        .addConverterFactory(GsonConverterFactory.create()).build()

    // Crear instancias de los servicios API
    val productoApiService = retrofit.create(ProductApiService::class.java)


    val navController = rememberNavController()

    Scaffold(
    modifier = Modifier.padding(top = 40.dp),
    topBar = { BarraSuperior(navController = navController,
        onBackPressed = { navController.popBackStack() }) },
    bottomBar = { BarraInferior(navController) },
    content = { paddingValues ->
        Contenido(
            paddingValues,
            navController = navController,
            productoApiService = productoApiService,
        )
    }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(navController: NavHostController, onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = painterResource(id = R.drawable.storekeeperig), // Reemplaza con tu icono
                    contentDescription = "Icono de StoreKeeper",
                    modifier = Modifier
                        .size(88.dp) // Ajusta el tamaño del icono a 48.dp para que sea más visible
                        .padding(start = 4.dp) // Espaciado para el icono
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Espaciado entre el icono y el título
                Text(
                    text = "StoreKeeper",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center // Asegura que el texto esté centrado
                )
            }

        },
        navigationIcon = {
            // Muestra la flecha de retroceso solo si no estás en la pantalla principal
            val currentBackStackEntry = navController.currentBackStackEntryAsState().value
            if (currentBackStackEntry?.destination?.route != "inicio") { // Reemplaza "inicio" con tu ruta principal
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retroceder")
                }
            }
        },
        actions = {
            // Coloca el icono en el extremo izquierdo

        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF2196F3)
        )
    )
}




@Composable
fun BarraInferior(navController: NavHostController) {
    NavigationBar(
        containerColor = Color.LightGray
    ) {

        NavigationBarItem(
            icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Productos") },
            label = { Text("Productos") },
            selected = navController.currentDestination?.route == "productos",
            onClick = { navController.navigate("productos") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = navController.currentDestination?.route == "inicio",
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Category, contentDescription = "Categorías") },
            label = { Text("Categorías") },
            selected = navController.currentDestination?.route == "categorias",
            onClick = {  }
        )
    }
}

@Composable
fun Contenido(
    pv: PaddingValues,
    navController: NavHostController,
    productoApiService: ProductApiService
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(pv)
    ) {
        NavHost(
            navController = navController,
            startDestination = "inicio" // Ruta de inicio
        ) {
            composable("inicio") { ScreenInicio() }

            // Vistas para productos
            composable("productos") {
                ProductosListado(navController, productoApiService)
            }
            composable("AddProduct") {
                AddProductScreen(
                    onProductCreated = { newProduct ->
                        // Lógica para manejar la creación del producto
                    },
                    productApiService = productoApiService,
                    navController = navController
                )
            }
            composable(
                "editp/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                EditProductScreen(
                    productId = productId,
                    onProductUpdated = { updatedProduct ->
                        // Lógica después de actualizar el producto, como navegar a otra pantalla
                        navController.navigateUp() // Vuelve a la pantalla anterior
                    },
                    productApiService = productoApiService,
                    navController = navController
                )
            }

        }
    }

}

@Composable
fun ScreenInicio() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Logo()
            Spacer(modifier = Modifier.height(32.dp))
            WelcomeTitle()
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeMessage()
        }
    }
}

@Composable
fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.storekeeperig),
        contentDescription = "Logo de StoreKeeper",
        modifier = Modifier.size(120.dp)
    )
}

@Composable
fun WelcomeTitle() {
    Text(
        text = "Bienvenido a StoreKeeper",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun WelcomeMessage() {
    Text(
        text = "Administra tu inventario de forma fácil y rápida.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )
}
