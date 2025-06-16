import xlrd, xlwt, random, requests

API_KEY = "AIzaSyAOcRUDXRJyOfVMSertvXAfiYN7ZRg7UZM"

def cerca_generi_google(isbn=None, titolo=None):
    query = ""
    if isbn:
        query = f"isbn:{isbn}"
    elif titolo:
        query = f"intitle:{titolo}"
    else:
        return "Genere Sconosciuto"
    url = f"https://www.googleapis.com/books/v1/volumes?q={query}&key={API_KEY}"
    resp = requests.get(url)
    if resp.status_code != 200:
        return "Genere Sconosciuto"
    data = resp.json()
    items = data.get("items")
    if not items:
        return "Genere Sconosciuto"
    # Prendi il primo risultato
    info = items[0].get("volumeInfo", {})
    cats = info.get("categories")
    if not cats:
        return "Genere Sconosciuto"
    return ", ".join(cats)

def aggiorna_file_libri(input_file, output_file):
    in_wb = xlrd.open_workbook(input_file)
    in_sheet = in_wb.sheet_by_index(0)
    out_wb = xlwt.Workbook()
    out_sheet = out_wb.add_sheet(in_sheet.name)

    # intestazioni
    for c in range(in_sheet.ncols):
        out_sheet.write(0, c, in_sheet.cell_value(0, c))

    for r in range(1, in_sheet.nrows):
        titolo = in_sheet.cell_value(r, 0)
        autore = in_sheet.cell_value(r, 1)
        isbn = in_sheet.cell_value(r, 2)
        # Copia titolo, autore, isbn
        out_sheet.write(r, 0, titolo)
        out_sheet.write(r, 1, autore)
        out_sheet.write(r, 2, isbn)

        # 1) Cerca generi su Google
        generi = cerca_generi_google(isbn=isbn, titolo=titolo)
        out_sheet.write(r, 3, generi)

        # 2) Valutazione casuale 1–5
        out_sheet.write(r, 4, random.randint(1,5))

        # 3) Stato lettura "si"/"no" → "LETTO"/"DA_LEGGERE"
        stato = in_sheet.cell_value(r, in_sheet.ncols - 1).strip().lower()
        stato_mod = "LETTO" if stato == "si" else ("DA_LEGGERE" if stato == "no" else stato)
        out_sheet.write(r, in_sheet.ncols - 1, stato_mod)

        # Copia eventuali altre colonne centrali
        for c in range(5, in_sheet.ncols - 1):
            out_sheet.write(r, c, in_sheet.cell_value(r, c))

    out_wb.save(output_file)

if __name__ == "__main__":
    aggiorna_file_libri("libri.xls", "libri_modificati.xls")
    print("Fatto! File salvato come libri_modificati.xls")
