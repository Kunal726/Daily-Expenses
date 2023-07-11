package com.kunalkulthe.dailyexpenses

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.kunalkulthe.dailyexpenses.data.Expense
import com.kunalkulthe.dailyexpenses.data.dao.ExpenseDao
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class UpdateExpense : Fragment() {

    private lateinit var imageE: ImageView
    private lateinit var catTable: TableLayout
    private var imageUriGlobal: Uri? = null
    private var expId: String? = null

    // Create an ActivityResultLauncher to handle the result of the image selection
    private val imageSelectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data

            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .into(imageE)

                imageUriGlobal = it
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        expId = arguments?.getString("expId")
        return inflater.inflate(R.layout.fragment_update_expense, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val expense: Expense? = ExpenseDao(requireContext().applicationContext).getExpenseById(expId.toString().toLong())

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateE = view.findViewById<EditText>(R.id.edit_date)
        val amountE = view.findViewById<EditText>(R.id.edit_amount)
        val descriptionE = view.findViewById<EditText>(R.id.edit_description)
        val categoryE = view.findViewById<EditText>(R.id.edit_category)

        imageE = view.findViewById(R.id.edit_image)
        catTable = view.findViewById(R.id.category_table)

        val saveB = view.findViewById<Button>(R.id.btn_save)
        val deleteB = view.findViewById<ImageButton>(R.id.btn_delete)

        expense?.let {
            dateE.setText(it.date)
            amountE.setText(it.amount.toString())
            categoryE.setText(it.category)
            descriptionE.setText(it.description)
            if (!it.image.contentEquals("".toByteArray()))
            {
                val bitmap = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                imageE.setImageBitmap(bitmap)
            }
        }


        categoryE.setOnClickListener { catTable.visibility = View.VISIBLE }
        categoryE.doOnTextChanged { _, _, _, _ -> categoryE.error = null }

        dateE.setOnClickListener {
            catTable.visibility = View.GONE

            val datePickerDialog = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = LocalDate.of(year, month + 1, day)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = date.format(formatter)
                    dateE.setText(formattedDate)
                }, year, month, day)

            datePickerDialog.show()
        }

        amountE.setOnClickListener {
            catTable.visibility = View.GONE
        }
        amountE.doOnTextChanged { _, _, _, _ -> amountE.error = null }

        descriptionE.setOnClickListener {
            catTable.visibility = View.GONE
        }

        imageE.setOnClickListener {
            catTable.visibility = View.GONE

            val drawable = imageE.drawable

            if (expense!!.image.contentEquals("".toByteArray()) && imageUriGlobal == null) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imageSelectionLauncher.launch(intent)
            } else {
                ShowImageSelectionDialog(drawable)
            }


        }

        for (i in 0 until catTable.childCount) {
            val tableRow: TableRow = catTable.getChildAt(i) as TableRow

            for (j in 0 until tableRow.childCount) {
                val textView: TextView = tableRow.getChildAt(j) as TextView

                textView.setOnClickListener {
                    categoryE.setText(textView.text.toString())
                }
            }
        }

        saveB.setOnClickListener {
            catTable.visibility = View.GONE
            val ret = updateData(dateE, amountE, categoryE, descriptionE, imageUriGlobal)
            if (ret == 0)
            {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        deleteB.setOnClickListener {
            catTable.visibility = View.GONE
            ExpenseDao(requireContext().applicationContext).deleteExpense(expense!!)
            requireActivity().supportFragmentManager.popBackStack()
        }


    }

    private fun ShowImageSelectionDialog(drawable: Drawable) {
        val options = arrayOf("View Image", "Change Image")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Image Options")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if(imageUriGlobal != null)
                    {
                        val bytearray = getByteArrayFromImageUri(requireContext().applicationContext, imageUriGlobal!!)
                        val bitmap = BitmapFactory.decodeByteArray(bytearray, 0, bytearray?.size ?: 0)
                        viewImage(bitmap)
                    }
                   else
                        viewImage(drawable.toBitmap())
                    dialog.dismiss()
                }
                1 -> {
                    changeImage()
                    dialog.dismiss()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()


    }

    private fun viewImage(bitmap: Bitmap) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_enlarged_image)

        val imageView = dialog.findViewById<ImageView>(R.id.enlargedImageView)
        imageView.setImageBitmap(bitmap)

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun changeImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageSelectionLauncher.launch(intent)
    }

    private fun updateData(dateE: EditText, amountE: EditText, categoryE: EditText, descriptionE: EditText, imageUriGlobal: Uri?): Int {
        val date = dateE.text.toString()
        val amount = amountE.text.toString()
        val category = categoryE.text.toString()
        val description = descriptionE.text.toString()

        if (amount.isBlank() || amount.isEmpty())
            amountE.error = "Amount Cannot Be Empty"

        else if (category.isBlank() || category.isEmpty())
            categoryE.error = "Category Cannot Be Empty Please Select One"

        else {
            val image: ByteArray
            if (imageUriGlobal != null)
                image = getByteArrayFromImageUri(requireContext().applicationContext, imageUriGlobal)!!
            else
                image = "".toByteArray()

            ExpenseDao(requireContext().applicationContext).updateExpense(
                Expense(
                    userId = UserPreferences(requireContext().applicationContext).getUserId().toString(),
                    category = category,
                    description = description,
                    amount = amount.toLong(),
                    image = image,
                    date = date,
                    isSynced = false,
                    expId = expId.toString().toLong()
                )
            )

            Toast.makeText(
                requireContext().applicationContext,
                "Data Updated",
                Toast.LENGTH_SHORT
            ).show()

            val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>().build()
            WorkManager.getInstance(requireContext().applicationContext).enqueue(workRequest)

            return 0
        }

        return 1
    }


    fun getByteArrayFromImageUri(context: Context, imageUri: Uri): ByteArray? {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }
            inputStream?.close()
            return byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
