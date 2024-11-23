package com.example.storekeeper.ui.theme.Productos

import com.example.storekeeper.ui.theme.Categoria.CategoryModel
import com.example.storekeeper.ui.theme.RFID.RFIDModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductApiService {
    @GET("api/productos")
    suspend fun selectProducts(): List<ProductModel>

    @GET("api/productos/{id}")
    suspend fun selectProduct(@Path("id") id: Int): Response<ProductModel>

    @Headers("Content-Type: application/json")
    @POST("api/productos")
    suspend fun insertProduct(@Body product: ProductModel): Response<ProductModel>

    @PUT("api/producto/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: ProductModel): Response<ProductModel>

    @DELETE("api/producto/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<ProductModel>

    // Nueva función para obtener categorías
    @GET("api/categorias")
    suspend fun selectCategories(): List<CategoryModel>

    // Nueva función para obtener etiquetas RFID
    @GET("api/uid")
    suspend fun selectRFIDs(): List<RFIDModel>
}
