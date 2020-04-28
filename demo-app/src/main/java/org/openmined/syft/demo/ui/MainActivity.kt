package org.openmined.syft.demo.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.chart_acc
import kotlinx.android.synthetic.main.activity_main.chart_loss
import kotlinx.android.synthetic.main.activity_main.progressBar
import org.openmined.syft.demo.BuildConfig
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityMainBinding
import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "MainActivity"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val modelVersion : String? = intent.getStringExtra(SetupDataActivity.MODEL_VERSION_DATA);
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
//        setSupportActionBar(toolbar)
        binding.lifecycleOwner = this
        modelVersion?.let { binding.viewModel = initiateViewModel(BuildConfig.SYFT_BASE_URL, it) }

        (binding.viewModel as FederatedCycleViewModel).processState.observe(
            this,
            Observer { onProcessStateChanged(it) }
        )

        (binding.viewModel as FederatedCycleViewModel).processDataLoss.observe(
            this,
            Observer { onProcessDataLoss(it) }
        )

        (binding.viewModel as FederatedCycleViewModel).processDataAcc.observe(
            this,
            Observer { onProcessDataAcc(it) }
        )

        (binding.viewModel as FederatedCycleViewModel).logger.observe(this, Observer {
            val loggers = it.split('\n');
            Toast.makeText(this, loggers.last(), Toast.LENGTH_SHORT).show()
        })

        (binding.viewModel as FederatedCycleViewModel).startCycle()
    }

    private fun onProcessDataLoss(it: ProcessData?) {
        processDataLoss(it ?: ProcessData(emptyList()))
    }

    private fun onProcessDataAcc(it: ProcessData?) {
        processDataAcc(it ?: ProcessData(emptyList()))
    }

    private fun onProcessStateChanged(processState: ProcessState?) {
        when (processState) {
            ProcessState.Hidden -> progressBar.visibility = ProgressBar.GONE
            ProcessState.Loading -> progressBar.visibility = ProgressBar.VISIBLE
        }
    }

    private fun processDataLoss(processState: ProcessData) {
        // TODO do with fold
        val entries = mutableListOf<Entry>()
        processState.data.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }
        val dataSet = LineDataSet(entries, "loss")
        dataSet.color = resources.getColor(R.color.colorAccent)
        val lineData = LineData(dataSet)
        chart_loss.data = lineData
        chart_loss.invalidate()
    }

    private fun processDataAcc(processState: ProcessData) {
        // TODO do with fold
        val entries = mutableListOf<Entry>()
        processState.data.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }
        val dataSet = LineDataSet(entries, "accuracy")
        dataSet.color = resources.getColor(R.color.colorPrimary)
        val lineData = LineData(dataSet)
        chart_acc.data = lineData
        chart_acc.invalidate()
    }

    private fun initiateViewModel(baseUrl: String, modelVersion : String): FederatedCycleViewModel {
        val networkingSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }
        val computeSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }

        val localConfiguration = LocalConfiguration(
            filesDir.absolutePath,
            filesDir.absolutePath,
            filesDir.absolutePath
        )
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        return MainViewModelFactory(
            baseUrl,
            "auth",
            dataRepository,
            networkingSchedulers,
            computeSchedulers,
            localConfiguration,
             modelVersion
        ).create(FederatedCycleViewModel::class.java)
    }
}
