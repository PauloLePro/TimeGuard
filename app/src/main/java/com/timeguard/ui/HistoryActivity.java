package com.timeguard.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.timeguard.R;
import com.timeguard.data.RuleRepository;
import com.timeguard.ui.adapters.HistoryAdapter;

public class HistoryActivity extends AppCompatActivity {

    private RuleRepository repo;
    private HistoryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(R.string.history_title);

        repo = RuleRepository.getInstance(this);

        Button btnClear = findViewById(R.id.btnClear);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvHistory);

        adapter = new HistoryAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        repo.observeHistory().observe(this, logs -> {
            adapter.submitList(logs);
            boolean empty = logs == null || logs.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        btnClear.setOnClickListener(v -> confirmClear());
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.history_clear)
                .setPositiveButton(R.string.yes, (d, w) -> repo.clearHistoryAsync())
                .setNegativeButton(R.string.no, null)
                .show();
    }
}

