package com.calite.shoplist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ListActivity extends AppCompatActivity {
    private EditText etNewProduct;
    private Button bSaveProduct;
    private Button bDeleteAll;
    private TextView tvTitleList;
    private SharedPreferences preferences;
    private String fileName;
    private Button bDeleteSelected;
    //los datos se almacenan en data/data/nombre_app/shared_prefs
    private ListView lvProducts;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> products = new ArrayList<>();
    //nombre producto
    private EditText etName;
    private String newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //cargamos nombre del fichero pasado por la anterior intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fileName = extras.getString("fileName").replaceAll(".xml", "");
        }

        tvTitleList = findViewById(R.id.tvTitleList);
        tvTitleList.setText(fileName);

        loadProducs();

        //list view con adapter sencillito
        lvProducts = findViewById(R.id.lvProducts);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, products);
        lvProducts.setAdapter(this.arrayAdapter);

        //edit text con boton para guardar productos en la lista
        bSaveProduct = findViewById(R.id.bSaveProduct);
        etNewProduct = findViewById(R.id.etNewProduct);
        bSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNewProduct.getText().length() > 0) {
                    products.add(etNewProduct.getText().toString());
                    etNewProduct.setText("");
                    Toast.makeText(getApplicationContext(), "Product Saved", Toast.LENGTH_SHORT).show();
                    saveProducts();
                    arrayAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ListActivity.this, "Name should´t be emtpy", Toast.LENGTH_LONG).show();
                }

            }
        });
        //borramos lista / reset
        bDeleteAll = findViewById(R.id.bDeleteAll);
        bDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                products.clear();
                arrayAdapter.notifyDataSetChanged();
                deleteAll();
            }
        });

        //borrado
        //abrimos menu contextual
        lvProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //hay que declarar dos metodos, onCreateContextMenu y onContextTtemSelected
                //menu contextual
                registerForContextMenu(lvProducts);

                return false;
            }
        });

    }

    //para que se guarde cuando cerremos de cualquier manera
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveProducts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveProducts();
    }

    //cargara productos
    private void loadProducs() {
        preferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //esto devuelve un mapa, por lo tanto podemos sacar el size, para el for
        //recogera cada entry del lista_productos
        Map m = preferences.getAll();
        for (int i = 0; i < m.size(); i++) {
            //recogemos cada producto y almacenamos en el array
            String producto = preferences.getString("product" + i, "");
            products.add(producto);
        }
    }

    private void saveProducts() {
        //vamos almacenando los productos en el fichero
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        for (int i = 0; i < products.size(); i++) {
            editor.putString("product" + i, products.get(i));
        }
        editor.commit();
    }

    private void deleteAll() {
        //limpiamos
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose an option: ");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_list, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //return super.onContextItemSelected(item);
        //obtenemos informacion del menu clicado
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()) {
            case R.id.edit:
                //nos traemos la vista del login
                View add_list = View.inflate(ListActivity.this, R.layout.add_list, null);
                etName = add_list.findViewById(R.id.etName);
                //creamos el alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Change Name of " + products.get(info.position));
                builder.setView(add_list);
                //asociamos evento al boton
                builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //almacenamos los datos de la caja de texto en variable
                        newName = etName.getText().toString();
                        //añadimos la lista al list view y creamos el fichero
                        products.set(info.position,newName);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //NADA
                    }
                });
                builder.create().show();
                break;
            case R.id.delete:
                Toast.makeText(this, "removed " + products.get(info.position), Toast.LENGTH_SHORT).show();
                products.remove(info.position);
                arrayAdapter.notifyDataSetChanged();
                saveProducts();
                break;
        }
        return super.onContextItemSelected(item);
    }
}