package com.example.storekeeper.ui.theme.Productos

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.storekeeper.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosListado(navController: NavHostController, servicio: ProductApiService) {
    var listaProducts by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var categoriasMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var rfidMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    var productStockMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    // Llamada al servicio para obtener productos, categorías y etiquetas RFID
    LaunchedEffect(Unit) {
        try {
            // Obtiene inicialmente los productos, categorías y RFID
            val products = servicio.selectProducts()
            listaProducts = products

            // Crear mapa inicial de stock
            productStockMap = products.associate { it.id to it.stock }

            val categories = servicio.selectCategories()
            categoriasMap = categories.associate { it.id to it.name }

            val rfids = servicio.selectRFIDs()
            rfidMap = rfids.associate { it.id to it.idTag }

            // Comienza el polling
            while (true) {
                delay(1000) // Espera 10 segundos
                val updatedProducts = servicio.selectProducts() // Consulta la API nuevamente

                // Crear una lista para reorganizar los productos
                val updatedList = mutableListOf<ProductModel>()

                updatedProducts.forEach { updatedProduct ->
                    val previousStock = productStockMap[updatedProduct.id]
                    if (previousStock != null && previousStock != updatedProduct.stock) {
                        // Agregar productos actualizados al inicio
                        updatedList.add(0, updatedProduct)
                    } else {
                        // Agregar productos no actualizados al final
                        updatedList.add(updatedProduct)
                    }
                }

                // Actualizar el mapa de stock
                productStockMap = updatedProducts.associate { it.id to it.stock }

                // Asignar la lista reorganizada
                listaProducts = updatedList
            }

        } catch (e: Exception) {
            Log.e("API", "Error al obtener datos: ${e.message}")
        }
    }

    // El resto de tu código permanece igual
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Listado de Productos") },
            actions = {
                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtrar Producto")
                }
                IconButton(onClick = { navController.navigate("AddProduct") }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                }
            },
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                BottomSheetContent(
                    categoriasMap = categoriasMap,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onApplyFilter = {
                        scope.launch {
                            bottomSheetState.hide()
                            showBottomSheet = false
                            // Filtrar productos por la categoría seleccionada
                            listaProducts = if (selectedCategory != null) {
                                listaProducts.filter { it.category == selectedCategory }
                            } else {
                                servicio.selectProducts() // Resetear a todos los productos si no hay categoría seleccionada
                            }
                        }
                    }
                )
            }
        }

        // Lista de productos
        if (listaProducts.isEmpty()) {
            Text(text = "No hay productos disponibles", modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(modifier = Modifier.padding(4.dp)) {
                items(listaProducts) { product ->
                    // Verificamos el stock y asignamos el color correspondiente
                    val highlightColor = Color.Yellow
                    val defaultColor = if (product.stock <= 50) Color(0xFFFFCDD2) else Color(0xFFAEEFC5)
                    val cardColor = if (productStockMap[product.id] != product.stock) highlightColor else defaultColor

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(cardColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen circular del producto
                            Image(
                                painter = rememberAsyncImagePainter(model = product.img),
                                contentDescription = "Imagen del producto",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )


                            Spacer(modifier = Modifier.width(16.dp))

                            // Column con los detalles del producto
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Price: $${product.price}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val categoryName = categoriasMap[product.category] ?: "Desconocido"
                                Text(text = "Category: $categoryName", style = MaterialTheme.typography.bodySmall)
                                val rfidTag = product.idNFC?.let { rfidMap[it] } ?: "Sin etiqueta NFC"
                                Text(text = "RFID Tag: $rfidTag", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Stock y botones
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                        .background(if (product.stock > 10) Color.Green else Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${product.stock}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black
                                    )
                                }
                                IconButton(onClick = { navController.navigate("editp/${product.id}") }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Producto")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BottomSheetContent(
    categoriasMap: Map<Int, String>,
    selectedCategory: Int?,
    onCategorySelected: (Int?) -> Unit,
    onApplyFilter: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filtrar por categoría", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        categoriasMap.forEach { (id, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(id) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedCategory == id,
                    onClick = { onCategorySelected(id) }
                )
                Text(name, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onApplyFilter, modifier = Modifier.align(Alignment.End)) {
            Text("Aplicar Filtro")
        }
    }
}



