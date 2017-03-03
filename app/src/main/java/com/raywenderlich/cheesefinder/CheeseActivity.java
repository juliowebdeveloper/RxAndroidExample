/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.cheesefinder;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class CheeseActivity extends BaseSearchActivity {

    private Observable<String> createTextChangeObservable() {
        Observable<String> textChangeObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                final TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        emitter.onNext(s.toString());
                    }
                };

                mQueryEditText.addTextChangedListener(watcher);
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        mQueryEditText.removeTextChangedListener(watcher);
                    }
                });
            }
        });
        //Filtro para só ser chamado após terem mais de 2 caracteres.
        return textChangeObservable.filter(new Predicate<String>() {
            @Override
            public boolean test(String query) throws Exception {
                return query.length() > 2;
            }
        });
    }

    private Observable<String> createButtonClickObservable(){
        //Metodo declarado que retorna um observable que emitirá strings
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                mSearchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emitter.onNext(mQueryEditText.getText().toString()); //Quando clicar a chamada onNext no emitter será feita
                            //Passando o valor do mQueryEditText
                    }
                });

                emitter.setCancellable(new Cancellable() { //Para remover a referencia.
                    @Override
                    public void cancel() throws Exception {
                        mSearchButton.setOnClickListener(null);
                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Observable<String> searchTextObservable = createTextChangeObservable();

        // Observable<String> searchTextObservable = createButtonClickObservable();
        //Cria um observable chamadno o metodo que acabei de criar e atribuindo à uma variable
       /* searchTextObservable.subscribe(new Consumer<String>() { //Fazendo o subscribe passando um Consumer simples
            @Override
            public void accept(String query) throws Exception { //Sobreescrevendo o Accept
                showResult(mCheeseSearchEngine.search(query));
            }
        });*/
        searchTextObservable.observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        showProgressBar();
                    }
                })

                .map(new Function<String, List<String>>() {
            @Override
            public List<String> apply(String query) throws Exception {
                return mCheeseSearchEngine.search(query);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> result) throws Exception {
                        hideProgressBar();
                        showResult(result);
                    }
                });
    }



   /* subscribeOn is supposed to be called only once in the chain of operators. If it’s not, the first call wins. subscribeOn specifies the thread on which the observable will be subscribed (i.e. created). If you use observables that emit events from Android View, you need to make sure subscription is done on the Android UI thread.
    On the other hand, it’s okay to call observeOn as many times as you want in the chain. observeOn specifies the thread on which the next operators in the chain will be executed. For example:
            myObservable // observable will be subscribed on i/o thread
                    .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(*//* this will be called on main thread... *//*)
    .doOnNext(*//* ...and everything below until next observeOn *//*)
    .observeOn(Schedulers.io())
            .subscribe(*//* this will be called on i/o thread *//*);*/
}
