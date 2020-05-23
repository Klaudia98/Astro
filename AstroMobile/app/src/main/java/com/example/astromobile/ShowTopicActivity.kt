package com.example.astromobile

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.astromobile.adapters.CommentsAdapter
import com.example.astromobile.apiclient.ApiClient
import com.example.astromobile.models.Comment
import com.example.astromobile.models.Topic
import com.example.astromobile.services.AuthService
import kotlinx.android.synthetic.main.activity_forum.*
import kotlinx.android.synthetic.main.activity_show_topic.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

class ShowTopicActivity : AppCompatActivity() {

    private val apiClient = ApiClient()
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_topic)
        supportActionBar?.hide()

        authService = AuthService.getAuthService()!!

        val id: Int = intent.getIntExtra("id", 0)

        addComment.setOnClickListener {
            if(authService.isLogged()){
                val intent = Intent(this@ShowTopicActivity, AddCommentActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            else{
                val builder = AlertDialog.Builder(this, R.style.InfoAlert)
                builder.setTitle("Forum")
                builder.setMessage("Musisz się zalogować!")
                builder.setIcon(R.drawable.ic_info_outline_black_24dp)
                builder.setPositiveButton("Rejestruj") { _, _ ->
                    startActivity(Intent(this@ShowTopicActivity, RegisterActivity::class.java))
                }
                builder.setNegativeButton("Zaloguj") { _, _ ->
                    startActivity(Intent(this@ShowTopicActivity, LoginActivity::class.java))
                }
                builder.show()
            }
        }

        var adapter = CommentsAdapter(this, arrayListOf())
        val listItems: ArrayList<Comment> = arrayListOf()

        CoroutineScope(IO).launch {
            val response: Response = apiClient.getTopic(authService.getLoggedUserToken()!!.token, id)

            when (response.code) {
                200 -> {

                    val item: Topic = apiClient.getTopicData(response.body?.string())

                    for(item in item.comments){
                        listItems.add(item)
                    }

                    withContext(Dispatchers.Main){
                        comments.adapter = adapter
                        topic.text = item.title
                        rate.text = item.rate.toString()
                        date.text = item.date
                        author.text = item.user.userName
                    }
                }
                401 -> {
                    startActivity(Intent(this@ShowTopicActivity, LoginActivity::class.java))
                }
                else -> {
                    val builder = AlertDialog.Builder(this@ShowTopicActivity, R.style.InfoAlert)
                    builder.setTitle("Forum")
                    builder.setMessage("Wystąpił nie oczekiwany bład!")
                    builder.setIcon(R.drawable.ic_info_outline_black_24dp)
                    builder.setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(this@ShowTopicActivity, ConnectionCheckActivity::class.java))
                    }
                    builder.show()
                }
            }
        }

        adapter = CommentsAdapter(this, listItems)
        comments.adapter = adapter
    }
}