package com.cmpt362.blissful

class toBedeleted {
}

//package com.cmpt362.blissful
//
//import android.content.DialogInterface
//import android.content.Intent
//import android.os.Bundle
//import android.text.InputFilter
//import android.text.Spanned
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.cmpt362.blissful.ui.social.GoogleLoginActivity
//import com.firebase.ui.firestore.FirestoreRecyclerAdapter
//import com.firebase.ui.firestore.FirestoreRecyclerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//
//
//data class User(
//    val entryName: String = "",
//    val userId: String = ""
//)
//
//class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//
//class MainActivity : AppCompatActivity() {
//    lateinit var rvUsers:RecyclerView
//    private companion object {
//        private const val TAG = "MainActivity"
//    }
//
//    private val db = Firebase.firestore
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main1)
//        auth = Firebase.auth
//
//        // Query the users collection
//        val query = db.collection("enteries")
//        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
//            .setLifecycleOwner(this).build()
//        val adapter = object: FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//                val view = LayoutInflater.from(this@MainActivity).inflate(android.R.layout.simple_list_item_2, parent, false)
//                return UserViewHolder(view)
//            }
//
//            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
//                val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
//                val tvEmojis: TextView = holder.itemView.findViewById(android.R.id.text2)
//                tvName.text = model.entryName
//                tvEmojis.text = model.userId
//            }
//        }
//        rvUsers = findViewById(R.id.rvUsers)
//        rvUsers.adapter = adapter
//        rvUsers.layoutManager = LinearLayoutManager(this)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.miLogout) {
//            Log.i(TAG, "Logout")
//            auth.signOut()
//            println("xd: hyy")
//            val logoutIntent = Intent(this, GoogleLoginActivity::class.java)
//            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(logoutIntent)
//        } else if (item.itemId == R.id.miEdit) {
//            showAlertDialog()
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun showAlertDialog() {
//        val editText = EditText(this)
//        val emojiFilter = EmojiFilter()
//        val lengthFilter = InputFilter.LengthFilter(9)
//        editText.filters = arrayOf(lengthFilter, emojiFilter)
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Update your emojis")
//            .setView(editText)
//            .setNegativeButton("Cancel", null)
//            .setPositiveButton("OK", null)
//            .show()
//        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
//            Log.i(TAG, "Clicked on positive button!")
//            val emojisEntered = editText.text.toString()
//            if (emojisEntered.isBlank()) {
//                Toast.makeText(this, "Cannot submit empty text", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val currentUser = auth.currentUser
//            if (currentUser == null) {
//                Toast.makeText(this, "No signed in user", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val nestedData: MutableMap<String, Any> = HashMap()
//            nestedData["entryName"] = "test1"
//            nestedData["userId"] = emojisEntered
//            db.collection("enteries").document("test")
//                .set(nestedData)
////                .document(currentUser.uid)
////                .update("emojis", emojisEntered)
//            dialog.dismiss()
//        }
//    }
//
//    inner class EmojiFilter : InputFilter {
//        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
//            if (source == null || source.isBlank()) {
//                return ""
//            }
//            Log.i(TAG, "Added text $source has length of ${source.length} characters")
//            for (inputChar in source) {
//                val type = Character.getType(inputChar)
//                Log.i(TAG, "Character type $type")
//            }
//            // The CharSequence being added is a valid emoji! Allow it to be added
//            return source
//        }
//
//    }
//}
