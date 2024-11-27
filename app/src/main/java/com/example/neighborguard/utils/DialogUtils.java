package com.example.neighborguard.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;

public class DialogUtils {
    public static void showMultiChoiceDialog(Context context, String title, String[] items, boolean[] selections,
                                             ArrayList<Integer> list, ArrayList<String> selectedItems,
                                             TextView targetView, String defaultText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setCancelable(false);

        builder.setMultiChoiceItems(items, selections, (dialog, which, isChecked) -> {
            if (isChecked) {
                list.add(which);
            } else {
                list.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder stringBuilder = new StringBuilder();
            selectedItems.clear();

            for (int i = 0; i < list.size(); i++) {
                String selectedItem = items[list.get(i)];
                selectedItems.add(selectedItem);
                stringBuilder.append(selectedItem);
                if (i < list.size() - 1) {
                    stringBuilder.append(", ");
                }
            }

            if (stringBuilder.length() > 0) {
                targetView.setText(stringBuilder.toString());
            } else {
                targetView.setText(defaultText);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Clear All", (dialog, which) -> {
            for (int i = 0; i < selections.length; i++) {
                selections[i] = false;
            }
            list.clear();
            selectedItems.clear();
            targetView.setText(defaultText);
        });

        builder.show();
    }
}
