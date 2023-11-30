package com.example.notepad

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.example.notepad.databinding.ActivityMainBinding
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = MainDb.getDb(this)

        db.getDao().getAllItem().asLiveData().observe(this) { list ->
            binding.notesContainer.removeAllViews() // Очищаем контейнер перед добавлением новых заметок

            list.forEach { note ->
                val noteView = TextView(this)
                val editButton = Button(this)
                val deleteButton = Button(this)

                noteView.text = "Тема: ${note.name}" +
                        "\n${note.content}"
                editButton.text = "Редактировать"
                deleteButton.text = "Удалить"

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


                binding.notesContainer.addView(noteView)
                binding.notesContainer.addView(editButton)
                binding.notesContainer.addView(deleteButton)
            }
        }

        binding.newNoteButton.setOnClickListener {
            val intent = Intent(this, CreateNewNoteActivity::class.java)
            intent.putExtra("noteId", -1) // Передача -1L для новой заметки
            startActivity(intent)
        }


    }


}