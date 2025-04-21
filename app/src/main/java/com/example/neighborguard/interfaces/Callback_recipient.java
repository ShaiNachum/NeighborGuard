package com.example.neighborguard.interfaces;


import com.example.neighborguard.model.User;

import java.util.ArrayList;

public interface Callback_recipient {
    void recipientClicked(double lat, double lng);

    // Method to update recipients on the map
    void updateMapRecipients(ArrayList<User> recipients);

}
