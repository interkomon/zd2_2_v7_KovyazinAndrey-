package com.example.pract2_2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.room.Room
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pract2_2.databinding.ActivityAddBinding

import kotlinx.coroutines.*

import org.json.JSONException
import org.json.JSONObject



class Add : Activity() {

    private lateinit var binding: ActivityAddBinding
    private lateinit var nameArtist:EditText

    private lateinit var titleArtist:TextView
    private lateinit var singer:TextView
    private lateinit var add:Button

    private lateinit var database: PlaylistDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameArtist = findViewById(R.id.nameArtist)

        titleArtist = findViewById(R.id.titleArtist)
        singer = findViewById(R.id.Singer)
        add = findViewById(R.id.add)


    }
    fun newplaylist(view: View) {
        database = Room.databaseBuilder(
            applicationContext,
            PlaylistDatabase::class.java,"playlist-database"
        ).build()

        val nameArtist = nameArtist.text.toString()
        if(nameArtist.isNotBlank()) {

            searchArtistId(nameArtist) { id ->
                getResualt(id)
            }
        }
        else
        {
            Toast.makeText(this, "Проверьте ввод данных :(",  Toast.LENGTH_SHORT).show()
        }
    }



    fun searchArtistId(artistName: String, callback: (String) -> Unit) {
        val apiKey = "VbokmShmIcueAGWMQNxFTIwylPfNdEVDHnsqxZWW"
        val searchUrl = "https://api.discogs.com/database/search?q=$artistName&type=artist&token=$apiKey"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            com.android.volley.Request.Method.GET, searchUrl,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val resultsArray = jsonObject.getJSONArray("results")

                    if (resultsArray.length() > 0) {
                        val artistInfo = resultsArray.getJSONObject(0)
                        val artistId = artistInfo.optInt("id")
                        callback(artistId.toString())
                    } else {
                        callback("")
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this,"Ошибка при загрузке данных",Toast.LENGTH_SHORT).show()
                    callback("")

                }
            },
            {
                Log.d("MyLog", "Volley error: ${it.message}")
                callback("") // Если произошла ошибка
            })

        queue.add(stringRequest)
    }


     fun getResualt(idArtist: String) {
        if (idArtist.isEmpty()) {
           Toast.makeText(this,"Ошибка при загрузке данных",Toast.LENGTH_SHORT).show()

        } else {

            val url = "https://api.discogs.com/artists/$idArtist?callback=Nirvana"
            val queue = Volley.newRequestQueue(this)

            val stringRequest = StringRequest(
                com.android.volley.Request.Method.GET, url,
                { response ->
                    // Обрабатываем JSONP-ответ
                    val jsonpData =
                        response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"))
                    try {
                        val jsonObject = JSONObject(jsonpData)
                        val artistData = jsonObject.getJSONObject("data")
                        val name = artistData.getString("name")
                        val profile = artistData.getString("profile")


                        var data = Playlist(name = name, singer = profile)
                        GlobalScope.launch (Dispatchers.IO) {
                            database.playlistDao().insertPlaylist(data)
                        }

                        titleArtist.text = name
                        singer.text = profile



                    } catch (e: JSONException) {
                        Toast.makeText(this,"Ошибка при загрузке данных",Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Log.d("MyLog", "Volley error hetresult: ${it.message}")
                })

            queue.add(stringRequest)
        }
    }


}

