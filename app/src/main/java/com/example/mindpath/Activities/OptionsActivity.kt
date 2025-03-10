        package com.example.mindpath.Activities

        import android.content.Intent
        import android.os.Bundle
        import android.widget.Button
        import androidx.appcompat.app.AppCompatActivity
        import com.example.mindpath.R

        class OptionsActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_options)

                findViewById<Button>(R.id.clientButton).setOnClickListener {
                    val intent = Intent(this, SigninActivity::class.java)
                    intent.putExtra("USER_TYPE", "CLIENT")
                    startActivity(intent)
                }

                findViewById<Button>(R.id.therapistButton).setOnClickListener {
                    val intent = Intent(this, SigninActivity::class.java)
                    intent.putExtra("USER_TYPE", "THERAPIST")
                    startActivity(intent)
                }
            }
        }

         // Need to remove and modify functionality