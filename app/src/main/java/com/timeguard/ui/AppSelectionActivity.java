package com.timeguard.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.timeguard.R;
import com.timeguard.data.MonitorRule;
import com.timeguard.data.RuleRepository;
import com.timeguard.helpers.Prefs;
import com.timeguard.ui.adapters.AppAdapter;
import com.timeguard.ui.model.InstalledAppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Liste des applications installées (launchables) + recherche.
 * Au clic : configure une règle.
 */
public class AppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "AppSelectionActivity";

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private SearchView searchView;
    private RecyclerView rvApps;
    private AppAdapter adapter;

    private RuleRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        setTitle(R.string.apps_title);

        repo = RuleRepository.getInstance(this);

        searchView = findViewById(R.id.searchView);
        rvApps = findViewById(R.id.rvApps);

        adapter = new AppAdapter(app -> {
            MonitorRule rule = new MonitorRule();
            rule.packageName = app.packageName;
            rule.appName = app.label;
            rule.limitMinutes = Prefs.getDefaultLimitMinutes(this);
            // 0 => utiliser la valeur par défaut (modifiable dans Paramètres).
            rule.cooldownMinutes = 0;
            rule.enabled = true;
            rule.lastNotifiedAtMs = 0L;

            RuleConfigDialog.showCreate(this, rule, saved -> {
                repo.saveRuleAsync(saved);
                // Retour simple à l’écran principal après ajout.
                finish();
            });
        });

        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        loadAppsAsync();
    }

    private void loadAppsAsync() {
        io.execute(() -> {
            List<InstalledAppInfo> apps = loadLaunchableApps();
            runOnUiThread(() -> adapter.submitList(apps));
        });
    }

    private List<InstalledAppInfo> loadLaunchableApps() {
        try {
            PackageManager pm = getPackageManager();
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> resolves = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
            List<InstalledAppInfo> out = new ArrayList<>();
            if (resolves != null) {
                for (ResolveInfo r : resolves) {
                    if (r.activityInfo == null) continue;
                    String pkg = r.activityInfo.packageName;
                    String label = r.loadLabel(pm) != null ? r.loadLabel(pm).toString() : pkg;
                    out.add(new InstalledAppInfo(label, pkg, r.loadIcon(pm)));
                }
            }

            Collections.sort(out, Comparator.comparing(a -> a.label.toLowerCase()));
            return out;
        } catch (Exception e) {
            Log.e(TAG, "loadLaunchableApps failed", e);
            return new ArrayList<>();
        }
    }
}
