package com.calite.shoplist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    //main
    private FloatingActionButton addList;
    private ListView lvShopLists;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> shopLists  = new ArrayList<>();

    //add_list layout
    private EditText etName;
    private String listName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadLists();

        //configuracion del adaptador y su listview
        lvShopLists = findViewById(R.id.lvLists);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, shopLists);
        lvShopLists.setAdapter(arrayAdapter);

        //boton de añadir
        addList = findViewById(R.id.addList);
        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nos traemos la vista del login
                View add_list = View.inflate(MainActivity.this, R.layout.add_list, null);
                etName = add_list.findViewById(R.id.etName);
                //creamos el alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add a new Shop List");
                builder.setView(add_list);
                //asociamos evento al boton
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //almacenamos los datos de la caja de texto en variable
                        listName = etName.getText().toString();
                        //añadimos la lista al list view y creamos el fichero
                        shopLists.add(listName);
                        SharedPreferences.Editor editor = getSharedPreferences(listName, MODE_PRIVATE).edit();
                        editor.apply();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //NADA
                    }
                });
                builder.create().show();
            }
        });
        //clic normal -> abre la lista
        lvShopLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //enviamos a la otra actividad con el nombre del fichero a editar
                Intent i = new Intent(getApplicationContext(), ListActivity.class);
                i.putExtra("fileName",shopLists.get(position));
                startActivity(i);
            }
        });
        //borrado simple
        lvShopLists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Do you want to delete " + shopLists.get(position));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //borramos fichero
                        String filePath = getApplicationContext().getFilesDir().getParent()+"/shared_prefs/" + shopLists.get(position);
                        File deletePrefFile = new File(filePath);
                        deletePrefFile.delete();
                        //quitamos de la lista
                        shopLists.remove(position);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // close dialog
                        dialog.cancel();
                    }
                });
                builder.create();
                builder.show();

                return true;
            }
        });

    }

    private void loadLists() {

        File prefsdir = new File(getApplicationInfo().dataDir,"shared_prefs");

        String[] aux = prefsdir.list();
        for(String s : aux) {
            if(!s.contains(".bak")) {
                shopLists.add(s.replaceAll(".xml", ""));
            }
        }

    }
}