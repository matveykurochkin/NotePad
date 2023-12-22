package com.example.notepad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notepad.databinding.CreateNewNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class CreateNewNoteActivity : AppCompatActivity() {

    lateinit var binding: CreateNewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateNewNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var noteId = intent.getIntExtra("noteId", -1)

        if (noteId != -1) {
            // Если редактируем существующую заметку, загрузите ее данные и отобразите в полях
            loadExistingNote()
        }

        binding.btnSave.setOnClickListener {
            saveNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadExistingNote() {
        GlobalScope.launch(Dispatchers.IO) {
            var noteId = intent.getIntExtra("noteId", -1)
            Log.d("в loade", "$noteId")
            val localNoteId = noteId // Сохраняем копию noteId
            val existingNote = MainDb.getDb(this@CreateNewNoteActivity).getDao().getById(localNoteId)
            existingNote?.let {
                runOnUiThread {
                    binding.edName.setText(it.name)
                    binding.edText.setText(it.content)
                }
            }
        }
    }

    private fun saveNote() {
        Log.d("SaveNoteActivity", "Save button clicked")
        var title = binding.edName.text.toString()
        var content = binding.edText.text.toString()

        if (title.isEmpty() && content.isNotEmpty()) {
            title = "Тема заметки"
        } else if (title.isNotEmpty() && content.isEmpty()) {
            content = "Текст заметки!"
        }

        if (title.isNotEmpty() && content.isNotEmpty()) {
            var noteId = intent.getIntExtra("noteId", -1)
            val db = MainDb.getDb(this)

            // для выполнения асинхронных операций
            GlobalScope.launch(Dispatchers.IO) {
                if (noteId == -1) {
                    // Если это новая заметка, создаем новый объект Item и вставьте его в базу данных
                    val newNote = Item(null, name = title, content = content)
                    Thread {
                        db.getDao().insertItem(newNote)
                    }.start()
                } else {
                    // Если это редактирование существующей заметки, обновите данные в базе данных
                    val existingNote = db.getDao().getById(noteId)
                    existingNote?.let {
                        it.name = title
                        it.content = content
                        it.creationDate = Date()
                        db.getDao().updateItem(it)
                    }
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(applicationContext, "Заметка не сохранена! Пожалуйста, заполните необходимые поля!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
