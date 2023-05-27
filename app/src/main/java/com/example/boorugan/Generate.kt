package com.example.boorugan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class Generate : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)

        val button = findViewById<Button>(R.id.btn_generate)
        button.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    txt2img()
                } catch (e: IOException) {
                    // handle crash here
                }
            }
        }
    }

    fun txt2img() {
        val client = OkHttpClient()

        val MEDIA_TYPE = "application/json".toMediaType()

        val prompt = findViewById<EditText>(R.id.et_input)

        val promptText = prompt.text.toString()

        val requestBody = """
            {
              "enable_hr": true,
              "denoising_strength": 0,
              "firstphase_width": 0,
              "firstphase_height": 0,
              "hr_scale": 2,
              "hr_upscaler": "4x_BooruGan_650k",
              "hr_second_pass_steps": 0,
              "hr_resize_x": 0,
              "hr_resize_y": 0,
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
              "send_images": true,
              "save_images": true,
              "alwayson_scripts": {}
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("http://192.168.8.132:7860/sdapi/v1/txt2img")
            .post(requestBody.toRequestBody(MEDIA_TYPE))
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

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

}