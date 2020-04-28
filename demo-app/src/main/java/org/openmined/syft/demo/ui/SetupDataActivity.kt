package org.openmined.syft.demo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivitySetupDataBinding

class SetupDataActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog;

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySetupDataBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_setup_data)
        binding.lifecycleOwner = this
        dialog = AlertDialog.Builder(this).create();
        binding.buttonStartFederateTraining.setOnClickListener(View.OnClickListener { v ->
            if (binding.textInputEditTextModelVersion.text.isNullOrEmpty()) {
                dialog.setTitle("Error");
                dialog.setMessage("Model version must not be null");
                dialog.show();
                return@OnClickListener
            }
            val intent: Intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MODEL_VERSION_DATA, binding.textInputEditTextModelVersion.text.toString())
            this.startActivity(intent)
        })
    }

    companion object {
        public const val MODEL_VERSION_DATA : String = "MODEL_VERSION_DATA";
    }
}
