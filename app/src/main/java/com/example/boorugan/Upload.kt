package com.example.boorugan

//Made by Talal

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Upload : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var button: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        imageView = findViewById(R.id.display_image)
        button = findViewById(R.id.btn_upload)
        button.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        val button = findViewById<Button>(R.id.btn_generate)
        button.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    img2img()
                } catch (e: IOException) {
                    // handle crash here
                }
            }
        }
    }


    fun img2img() {
        //Initializes OkHttpClient
        val client = OkHttpClient()

        val MEDIA_TYPE = "application/json".toMediaType()

        val imageView = findViewById<ImageView>(R.id.display_image)

        // Step 1: Convert ImageView to Bitmap
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap

        // Step 2: Convert Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        var base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        //Made by Bader
        base64String = base64String.replace("\n","")



        val prompt = findViewById<EditText>(R.id.et_input)

        val promptText = prompt.text.toString()

        //Sends a JSON file
        val requestBody = """
            {
              "init_images": [
                "data:image/png;base64,$base64String"
              ],
              "resize_mode": 0,
              "image_cfg_scale": 0,
              "prompt": "$promptText",
              "styles": [
                "string"
              ],
              "seed": -1,
              "subseed": -1,
              "subseed_strength": 0,
              "seed_resize_from_h": -1,
              "seed_resize_from_w": -1,
              "sampler_name": "Euler a",
              "batch_size": 1,
              "n_iter": 1,
              "steps": 50,
              "cfg_scale": 7,
              "width": 512,
              "height": 512,
              "restore_faces": false,
              "tiling": false,
              "do_not_save_samples": false,
              "do_not_save_grid": false,
              "negative_prompt": "string",
              "eta": 0,
              "s_churn": 0,
              "s_tmax": 0,
              "s_tmin": 0,
              "s_noise": 1,
              "override_settings": {},
              "override_settings_restore_afterwards": true,
              "script_args": [],
              "sampler_index": "Euler",
              "include_init_images": false,
              "send_images": true,
              "save_images": true,
              "alwayson_scripts": {}
            }
        """.trimIndent()

        //Selects the Server IP and Port with the txt2img API parameter in Stable Diffusion
        val request = Request.Builder()
            .url("http://192.168.8.132:7860/sdapi/v1/img2img")
            .post(requestBody.toRequestBody(MEDIA_TYPE))
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            //Saves the Response JSON from the server into Data folder in app
            val responseBody = response.body!!.string()
            val outputFile = File(filesDir, "response.json")

            FileOutputStream(outputFile).use { output ->
                output.write(responseBody.toByteArray())
            }

            runOnUiThread {
                // Read the saved JSON file
                val jsonContent = outputFile.readText()

                // Parse the JSON
                val jsonObject = JSONObject(jsonContent)

                // Get the "images" array
                val imagesArray = jsonObject.getJSONArray("images")

                // Get the first image from the array
                val imageBase64 = imagesArray.optString(0)

                // Convert Base64 to Bitmap
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                // Display the image in ImageView
                val imageView = findViewById<ImageView>(R.id.display_image)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

}