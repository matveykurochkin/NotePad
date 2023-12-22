package com.example.notepad

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.notepad.databinding.ActivityMainBinding
import androidx.lifecycle.asLiveData
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = MainDb.getDb(this)

        db.getDao().getAllItem().asLiveData().observe(this) { list ->
            binding.notesContainer.removeAllViews() // Очищаем перед добавлением новых заметок

            list.forEach { note ->
                val noteView = TextView(this)
                val editButton = Button(this)
                val deleteButton = Button(this)
                val buttonContainer = LinearLayout(this)
                buttonContainer.orientation = LinearLayout.HORIZONTAL

                noteView.text = "Тема: ${note.name}" +
                        "\n${note.content}" +
                        "\n${note.creationDate.toString()}"
                editButton.text = "Редактировать"
                deleteButton.text = "Удалить"

                val buttonLayoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )

                editButton.setTextAppearance(R.style.ButtonStyle)
                deleteButton.setTextAppearance(R.style.ButtonStyle)

                // Обработчик нажатия на кнопку "Редактировать"
                editButton.setOnClickListener {
                    val editIntent = Intent(this, CreateNewNoteActivity::class.java)
                    editIntent.putExtra("noteId", note.id)
                    startActivity(editIntent)
                }

                // Обработчик нажатия на кнопку "Удалить"
                deleteButton.setOnClickListener {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.apply {
                        setTitle("Подтверждение")
                        setMessage("Вы уверены, что хотите удалить эту заметку?")
                        setPositiveButton("Удалить") { _, _ ->
                            // Пользователь подтвердил удаление
                            GlobalScope.launch(Dispatchers.IO) {
                                db.getDao().deleteItem(note)
                            }
                        }
                        setNegativeButton("Отмена") { dialog, _ ->
                            // Пользователь отменил удаление
                            dialog.dismiss()
                        }
                        create().show()
                    }
                }

                buttonContainer.addView(editButton,buttonLayoutParams)
                buttonContainer.addView(deleteButton,buttonLayoutParams)

                binding.notesContainer.addView(noteView)
                binding.notesContainer.addView(buttonContainer)
            }
        }

        binding.newNoteButton.setOnClickListener {
            val intent = Intent(this, CreateNewNoteActivity::class.java)
            intent.putExtra("noteId", -1)
            startActivity(intent)
        }
    }
}