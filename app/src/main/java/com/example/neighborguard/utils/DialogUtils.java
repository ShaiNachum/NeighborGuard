package com.example.neighborguard.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.UserRoleEnum;

import java.util.ArrayList;
import java.util.HashMap;

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


    public static void showServicesMultiChoiceDialog(
            Context context,
            String title,
            String[] items,
            boolean[] selections,
            ArrayList<Integer> list,
            HashMap<String, MeetingAssistanceStatusEnum> selectedServices,
            TextView targetView,
            String defaultText,
            UserRoleEnum userRole,
            HashMap<String, MeetingAssistanceStatusEnum> currentServices) {

        // Create filtered arrays without "General Check"
        ArrayList<String> filteredServices = new ArrayList<>();
        ArrayList<Boolean> filteredSelections = new ArrayList<>();

        // Build filtered arrays
        for (int i = 0; i < items.length; i++) {
            if (!items[i].equals("General Check")) {
                filteredServices.add(items[i]);
                filteredSelections.add(selections[i]);
            }
        }

        String[] filteredItems = filteredServices.toArray(new String[0]);
        boolean[] tempSelections = new boolean[filteredSelections.size()];
        for (int i = 0; i < filteredSelections.size(); i++) {
            tempSelections[i] = filteredSelections.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setCancelable(false);

        builder.setMultiChoiceItems(filteredItems, tempSelections, (dialog, which, isChecked) -> {
            String serviceName = filteredItems[which];

            // Handle recipient's IN_PROGRESS services
            if (userRole == UserRoleEnum.RECIPIENT &&
                    currentServices != null &&
                    currentServices.get(serviceName) == MeetingAssistanceStatusEnum.IN_PROGRESS) {

                tempSelections[which] = false;
                ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                Toast.makeText(context,
                        "Cannot choose " + serviceName + " while it is in progress",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isChecked) {
                list.add(which);
            } else {
                list.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder stringBuilder = new StringBuilder();
            selectedServices.clear();

            // Always set "General Check" for volunteers
            if (userRole == UserRoleEnum.VOLUNTEER) {
                selectedServices.put("General Check", MeetingAssistanceStatusEnum.PROVIDE);
            } else {
                selectedServices.put("General Check", MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
            }

            // Process other services based on role
            for (int i = 0; i < filteredItems.length; i++) {
                String serviceName = filteredItems[i];

                if (userRole == UserRoleEnum.VOLUNTEER) {
                    // For volunteers: only PROVIDE or DO_NOT_PROVIDE
                    MeetingAssistanceStatusEnum status = tempSelections[i] ?
                            MeetingAssistanceStatusEnum.PROVIDE :
                            MeetingAssistanceStatusEnum.DO_NOT_PROVIDE;
                    selectedServices.put(serviceName, status);
                } else {
                    // For recipients
                    if (currentServices != null &&
                            currentServices.get(serviceName) == MeetingAssistanceStatusEnum.IN_PROGRESS) {
                        selectedServices.put(serviceName, MeetingAssistanceStatusEnum.IN_PROGRESS);
                    } else {
                        MeetingAssistanceStatusEnum status = tempSelections[i] ?
                                MeetingAssistanceStatusEnum.NEED_ASSISTANCE :
                                MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE;
                        selectedServices.put(serviceName, status);
                    }
                }

                // Build display string for selected services
                if (tempSelections[i]) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(serviceName);
                }
            }

            // Update UI
            if (stringBuilder.length() > 0) {
                targetView.setText(stringBuilder.toString());
            } else {
                targetView.setText(defaultText);
            }

            // Update original selections array
            int filteredIndex = 0;
            for (int i = 0; i < selections.length; i++) {
                if (!items[i].equals("General Check")) {
                    selections[i] = tempSelections[filteredIndex++];
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Clear All", (dialog, which) -> {
            for (int i = 0; i < selections.length; i++) {
                String serviceName = items[i];

                if (userRole == UserRoleEnum.VOLUNTEER) {
                    if (serviceName.equals("General Check")) {
                        selectedServices.put(serviceName, MeetingAssistanceStatusEnum.PROVIDE);
                    } else {
                        selectedServices.put(serviceName, MeetingAssistanceStatusEnum.DO_NOT_PROVIDE);
                    }
                } else {
                    if (currentServices != null &&
                            currentServices.get(serviceName) == MeetingAssistanceStatusEnum.IN_PROGRESS) {
                        selectedServices.put(serviceName, MeetingAssistanceStatusEnum.IN_PROGRESS);
                    } else {
                        selectedServices.put(serviceName, MeetingAssistanceStatusEnum.DO_NOT_NEED_ASSISTANCE);
                    }
                }

                if (!serviceName.equals("General Check")) {
                    selections[i] = false;
                }
            }
            list.clear();
            targetView.setText(defaultText);
        });

        builder.show();
    }
}
