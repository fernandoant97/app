package com.example.roomcoord;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

class MonitoringSessionViewHolder extends RecyclerView.ViewHolder {
    private final TextView sessionNameItemView;
    private final TextView sessionDateTimeItemView;
    private final ImageButton optionsButton;

    private MonitoringSessionViewHolder(View itemView) {
        super(itemView);
        sessionNameItemView = itemView.findViewById(R.id.sessionNameTextView);
        sessionDateTimeItemView = itemView.findViewById(R.id.sessionDateTimeTextView);
        optionsButton = itemView.findViewById(R.id.optionsButton);

    }


    public void bind(String sessionName, String sessionDateTime) {
        sessionNameItemView.setText("Nome sessione: " + sessionName);
        sessionDateTimeItemView.setText("Data e ora: " + sessionDateTime);
    }
    public void setOptionsButtonClickListener(View.OnClickListener listener) {
        optionsButton.setOnClickListener(listener);
    }


    static MonitoringSessionViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.monitoring_session_recyclerview_item, parent, false);
        return new MonitoringSessionViewHolder(view);
    }
}

public class MonitoringSessionListAdapter extends ListAdapter<MonitoringSession, MonitoringSessionViewHolder> {
    private final AppRepository appRepository;
    public MonitoringSessionListAdapter(@NonNull DiffUtil.ItemCallback<MonitoringSession> diffCallback, Context context) {
        super(diffCallback);
        this.appRepository = new AppRepository((Application) context);
    }

    @Override
    public MonitoringSessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MonitoringSessionViewHolder.create(parent);

    }
    private PopupWindow popupWindow;

    @Override
    public void onBindViewHolder(MonitoringSessionViewHolder holder, int position) {
        MonitoringSession current = getItem(position);
        holder.bind(current.getSessionName(), current.getSessionDateTime());

        holder.setOptionsButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    showPopupMenu(v, current);
                }
            }
        });

        }

    private void showPopupMenu(View v, MonitoringSession current) {
        LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_menu, null);

        popupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        customView.findViewById(R.id.menu_view_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIntent = new Intent(v.getContext(), PotholesActivity.class);
                logIntent.putExtra("SESSION_ID", current.getSessionId());
                v.getContext().startActivity(logIntent);
                popupWindow.dismiss();
            }
        });

        customView.findViewById(R.id.menu_view_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aggiungi qui il codice per aprire l'activity Maps o fare altre azioni
                popupWindow.dismiss();
            }
        });
        customView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(v.getContext(), current);
                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(v, 0, 0);
    }

    private void showDeleteConfirmationDialog(Context context, MonitoringSession current) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Conferma eliminazione");
        builder.setMessage("Sicuro di eliminare definitivamente la sessione di monitoraggio?");

        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Elimina la sessione di monitoraggio
                appRepository.deleteSessionAndPotholes(current.getSessionId());
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Annulla l'operazione
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    static class SessionDiff extends DiffUtil.ItemCallback<MonitoringSession> {
        @Override
        public boolean areItemsTheSame(@NonNull MonitoringSession oldItem, @NonNull MonitoringSession newItem) {
            return oldItem.getSessionId() == newItem.getSessionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MonitoringSession oldItem, @NonNull MonitoringSession newItem) {
            return oldItem.getSessionName().equals(newItem.getSessionName()) && oldItem.getSessionDateTime().equals(newItem.getSessionDateTime());
        }
    }


}

