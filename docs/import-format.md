# Portfolio Import Format

## Example source

The example file at `examples/import/carteira-export-example.csv` was generated from the uploaded `carteira-export.xlsx` workbook using a representative subset of its rows.

The workbook contains one worksheet named `Carteira` and uses Portuguese column names:

| Column | Meaning |
|---|---|
| `Data operação` | Operation date in `DD/MM/YYYY` format |
| `Categoria` | Source asset category |
| `Código Ativo` | Asset ticker or identifier |
| `Operação C/V` | `C` for purchase and `V` for sale |
| `Quantidade` | Quantity, using Brazilian number formatting |
| `Preço unitário` | Unit price, using Brazilian number formatting |
| `Corretora` | Broker or financial institution |
| `Corretagem` | Brokerage cost |
| `Taxas` | Other fees |
| `Impostos` | Taxes |
| `IRRF` | Withholding income tax |

## Import requirements

The future importer should:

1. Accept `.xlsx` and `.csv` files.
2. Detect the worksheet and header row.
3. Support Portuguese column names and configurable aliases.
4. Parse Brazilian dates (`DD/MM/YYYY`).
5. Parse Brazilian numeric values, including thousands separators and decimal commas.
6. Map `C` to `BUY` and `V` to `SELL`.
7. Preserve the original broker and fee fields.
8. Show a validation preview before persistence.
9. Detect possible duplicate transactions.
10. Require the user to confirm ambiguous asset categories or identifiers.

## Important distinction

The example is test data for the import workflow. It must not be automatically inserted into a real user's portfolio without an explicit import confirmation.
