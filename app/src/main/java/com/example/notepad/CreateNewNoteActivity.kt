package com.example.notepad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import com.example.notepad.Item
import com.example.notepad.MainDb
import com.example.notepad.databinding.CreateNewNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateNewNoteActivity : AppCompatActivity() {

    lateinit var binding: CreateNewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateNewNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var noteId = intent.getIntExtra("noteId", -1)
        Log.d("внутри блпблпюблп", "$noteId")// Получаем идентификатор заметки из интента

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
        val title = binding.edName.text.toString()
        val content = binding.edText.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            var noteId = intent.getIntExtra("noteId", -1)
            val db = MainDb.getDb(this)
            val localNoteId = noteId // Сохраняем копию noteId
            Log.d("внутри 1", "1")
            Log.d("внутри 11", "$localNoteId")

            // Запускаем корутину для выполнения асинхронных операций
            GlobalScope.launch(Dispatchers.IO) {
                if (noteId == -1) {
                    Log.d("внутри 2", "2")
                    // Если это новая заметка, создайте новый объект Item и вставьте его в базу данных
                    val newNote = Item(null, name = title, content = content)
                    Thread {
                        db.getDao().insertItem(newNote)
                    }.start()
                } else {

                    // Если это редактирование существующей заметки, обновите данные в базе данных
                    val existingNote = db.getDao().getById(noteId)
                    Log.d("внутри 3", "3")
                    existingNote?.let {
                        it.name = title
                        it.content = content
                        db.getDao().updateItem(it)
                    }
                }
            }
        } else {
            Log.d("внутри 4", "4")
            // Предупреждение о том, что поля должны быть заполнены
            // Вы можете здесь добавить логику обработки ошибок по вашему усмотрению
        }
    }
}
