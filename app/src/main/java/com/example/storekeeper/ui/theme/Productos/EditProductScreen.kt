package com.example.storekeeper.ui.theme.Productos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.storekeeper.ui.theme.Categoria.CategoryModel
import com.example.storekeeper.ui.theme.RFID.RFIDModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
import android.util.Base64

private fun convertUriToBase64(uri: Uri, context: Context): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes: ByteArray? = inputStream?.readBytes()
        // Si bytes es nulo, retorna nulo
        bytes ?: return null
        // Codifica bytes a Base64
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("ImageConversion", "Error al convertir URI a base64: ${e.message}")
        null
    }
}
@Composable
fun EditProductScreen(
    productId: Int,
    onProductUpdated: (ProductModel) -> Unit,
    productApiService: ProductApiService,
    navController: NavHostController
) {
    val context = LocalContext.current

    // Estado para almacenar la información del producto a editar
    var product by remember { mutableStateOf<ProductModel?>(null) }

    var name by remember { mutableStateOf("") }
    var imgUri by remember { mutableStateOf<Uri?>(null) }
    var imgBase64 by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var nfcId by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf<List<CategoryModel>>(emptyList()) }
    var rfids by remember { mutableStateOf<List<RFIDModel>>(emptyList()) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imgUri = uri
        imgUri?.let {
            imgBase64 = convertUriToBase64(it, context)
        }
    }

    // Cargar el producto y los datos de categorías y etiquetas RFID al iniciar la pantalla
    LaunchedEffect(productId) {
        val response = productApiService.selectProduct(productId)
        if (response.isSuccessful) {
            response.body()?.let {
                product = it
                name = it.name
                imgBase64 = it.img
                price = it.price.toString()
                description = it.description
                stock = it.stock.toString()
                categoryId = it.category.toString()
                nfcId = it.idNFC?.toString() ?: ""
            }
        }
        categories = productApiService.selectCategories()
        rfids = productApiService.selectRFIDs()
    }

    // Composable para mostrar la pantalla de edición de producto
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Editar Producto",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Campos de entrada para editar el producto
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar Imagen")
            }
            imgBase64?.let {
                Image(
                    painter = rememberImagePainter(imgUri ?: Uri.parse(it)),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Menú desplegable para seleccionar categoría
        item {
            var isCategoryMenuExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCategoryMenuExpanded = !isCategoryMenuExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedCategory = categories.find { it.id.toString() == categoryId }
                    Text(selectedCategory?.name ?: "Seleccionar categoría")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Abrir menú")
                }
                DropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            onClick = {
                                categoryId = category.id.toString()
                                isCategoryMenuExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            text = { Text(category.name) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Menú desplegable para seleccionar etiqueta RFID
        item {
            var isRFIDMenuExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isRFIDMenuExpanded = !isRFIDMenuExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedRFID = rfids.find { it.id.toString() == nfcId }
                    Text(selectedRFID?.idTag ?: "Seleccionar etiqueta RFID")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Abrir menú")
                }

                DropdownMenu(
                    expanded = isRFIDMenuExpanded,
                    onDismissRequest = { isRFIDMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rfids.forEach { rfid ->
                        DropdownMenuItem(
                            onClick = {
                                nfcId = rfid.id.toString()
                                isRFIDMenuExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            text = { Text(rfid.idTag) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón para actualizar producto
        item {
            Button(
                onClick = {
                    product?.let {
                        val updatedProduct = it.copy(
                            name = name,
                            img = imgBase64,
                            price = price.toDoubleOrNull() ?: 0.0,
                            description = description,
                            stock = stock.toIntOrNull() ?: 0,
                            category = categoryId.toIntOrNull() ?: 0,
                            idNFC = nfcId.toIntOrNull()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = productApiService.updateProduct(updatedProduct.id, updatedProduct)
                                if (response.isSuccessful) {
                                    onProductUpdated(updatedProduct)
                                } else {
                                    Log.e("EditProductScreen", "Error en la respuesta de la API: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                Log.e("EditProductScreen", "Error al manejar el producto: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
